package com.example.mutantGen;

public enum MutatorType {
    RFB, // remove finally block
    MWT, // modify wait timeout
    MST, // modify socket timeout
    RFET, // remove file existance throws
    RUL, // remove unlock
    RSB, // remove synchronized block




    // RLUL, // remove lock and unlock
    // RINT, //TODO remove exception throw statements in if (Obj == NULL) statements

}
