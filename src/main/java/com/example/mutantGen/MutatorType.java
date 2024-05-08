package com.example.mutantGen;

public enum MutatorType {
    RFB, // remove finally block
    MWT, // modify wait timeout
    RFET, // remove file existance throws
    RUL, // remove unlock
    RSB, // remove synchronized block
    // ---------------network----------------
    MNR, // modify network resource checks
    MNT, // modify network timeout
    RRC, // remove resource closes
    UNE, // upcast network exception

    // ---------------concurrency----------------
    RCS, // remove critical sections

    // RLUL, // remove lock and unlock
    // RINT, //TODO remove exception throw statements in if (Obj == NULL) statements

}
