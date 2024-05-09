package com.example.mutantGen;

import com.example.mutantGen.mutators.*;
import com.example.mutantGen.mutators.concurrency.*;
import com.example.mutantGen.mutators.network.*;

public class MutatorFactory {
    public static MutantGen getMutator(MutatorType type) {
        switch (type) {
            case RFB:
                return new RFB();
            case MWT:
                return new MWT();
            case RFET:
                return new RFET();
            case RUL:
                return new RUL();
            // network
            case MNR:
                return new MNR();
            case MNT:
                return new MNT();
            case RRC:
                return new RRC();
            case UNE:
                return new UNE();
            // concurrency
            case RCS:
                return new RCS();
            case NCS:
                return new NCS();
            case SCS:
                return new SCS();
            case RTS:
                return new RTS();
            default:
                return null;
        }
    }
}
