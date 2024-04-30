package com.example.mutantGen;

import com.example.mutantGen.mutators.*;
import com.example.mutantGen.mutators.network.*;

public class MutatorFactory {
    public static MutantGen getMutator(MutatorType type) {
        switch (type) {
            case RFB:
                return new RFB();
            case MWT:
                return new MWT();
            case MST:
                return new MST();
            case RFET:
                return new RFET();
            case RUL:
                return new RUL();
            case RSB:
                return new RSB();
            case UNE:
                return new UNE();
            default:
                return null;
        }
    }
}