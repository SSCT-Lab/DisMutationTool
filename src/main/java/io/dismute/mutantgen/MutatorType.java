package io.dismute.mutantgen;

public enum MutatorType {

    // ---------------network----------------
    MNR, // modify network resource checks
    MNT, // modify network timeout
    RRC, // remove resource closes
    UNE, // upcast network exception
    RNE, // remove network exception

    // ---------------concurrency----------------
    BCS, // broaden critical sections
    RCS, // remove critical sections
    NCS, // narrow down critical sections
    SCS, // split critical sections
    RTS, // remove concurrency thread's synchronization
    UCE, // upcast concurrency exception
    RCE, // remove concurrency exception
    MCT, // modify concurrency timeout

    // ---------------consistency----------------
    RCF, // remove consistency files' checks
    UFE, // upcast consistency files' exception
    RFE, // remove consistency files' exception

    // ---------------pit----------------
    // https://pitest.org/quickstart/mutators/
    //https://github.com/hcoles/pitest/tree/master/pitest/src/main/java/org/pitest/mutationtest/engine/gregor/mutators
    CDB,//Conditionals Boundary Mutator
    ICR,//Increments Mutator
    IVN,// Invert Negatives Mutator
    MAM,//Math Mutator
    NGC,//Negate Conditionals Mutator
    RTV,//Return Values Mutator
    VMC,//Void Method Calls


    // ---------------mujava----------------
    COR,//Conditional Operator Replacement mutants, https://github.com/jeffoffutt/muJava/blob/master/src/mujava/op/basic/COR.java
    AODU,//Arithmetic Operator Deletion (Unary), https://github.com/jeffoffutt/muJava/blob/master/src/mujava/op/basic/AODU.java
    VDL,// Variable DeLetion, https://github.com/jeffoffutt/muJava/blob/master/src/mujava/op/basic/VDL.java
    CDL,//Constants DeLetion, https://github.com/jeffoffutt/muJava/blob/master/src/mujava/op/basic/CDL.java
    AOIS,//Arithmetic Operator Insertion (Short-cut), https://github.com/jeffoffutt/muJava/blob/master/src/mujava/op/basic/AOIS.java
    ASRS,//Assignment Operator Replacement (short-cut), https://github.com/jeffoffutt/muJava/blob/master/src/mujava/op/basic/ASRS.java
    ROR,//Rational Operator Replacement, https://github.com/jeffoffutt/muJava/blob/master/src/mujava/op/basic/ROR.java

    // --------------deprecated----------------
    RFB, // remove finally block
    MWT, // modify wait timeout
    RFET, // remove file existance throws
    RUL, // remove unlock
    RSB, // remove synchronized block

}
