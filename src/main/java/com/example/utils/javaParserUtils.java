package com.example.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.IfStmt;

import java.util.ArrayList;
import java.util.List;

public class javaParserUtils {

    /**
     * 获取if语句的condition中所有的方法调用
     * 舍弃，ifStmt.findAll就可以了
     * @param ifStmt
     * @return
     */
    public static List<MethodCallExpr> extractMethodCalls(IfStmt ifStmt) {
        List<MethodCallExpr> methodCalls = new ArrayList<>();
        extractMethodCalls(ifStmt.getCondition(), methodCalls);
        return methodCalls;
    }

    private static void extractMethodCalls(Expression expr, List<MethodCallExpr> methodCalls) {
        if (expr instanceof MethodCallExpr) {
            methodCalls.add((MethodCallExpr) expr);
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            extractMethodCalls(binaryExpr.getLeft(), methodCalls);
            extractMethodCalls(binaryExpr.getRight(), methodCalls);
        } else if (expr instanceof UnaryExpr) {
            UnaryExpr unaryExpr = (UnaryExpr) expr;
            extractMethodCalls(unaryExpr.getExpression(), methodCalls);
        }
    }


//    public writeCuToFile(CompilationUnit cu, )
}
