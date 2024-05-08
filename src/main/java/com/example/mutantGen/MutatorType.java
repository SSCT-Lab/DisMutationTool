package com.example.mutantGen;

public enum MutatorType {
    RFB, // remove finally block
    MWT, // modify wait timeout
    MST, // modify socket timeout
    RFET, // remove file existance throws
    RUL, // remove unlock
    RSB, // remove synchronized block
    UNE, // upcast network exception
    RRC, // remove resource closes
    MNR, // modify network resource checks

    // RLUL, // remove lock and unlock
    // RINT, //TODO remove exception throw statements in if (Obj == NULL) statements

}
