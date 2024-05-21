package com.example.mutator;

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
    BCS, // broaden critical sections
    RCS, // remove critical sections
    NCS, // narrow down critical sections
    SCS, // split critical sections
    RTS, // remove concurrency thread's synchronization
    UCE, // upcast concurrency exception
    MCT, // modify concurrency timeout

    // ---------------consistency----------------
    RCF, // remove consistency files' checks
    UFE, // upcast consistency files' exception
}
