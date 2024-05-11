package com.example.mutantGen;

import com.example.mutantGen.mutators.concurrency.*;
import com.example.mutantGen.mutators.deprecated.*;
import com.example.mutantGen.mutators.network.*;
import com.example.mutantGen.mutators.consistency.*;

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
            case UCE:
                return new UCE();
            case MCT:
                return new MCT();
            case BCS:
                return new BCS();
            // consistency
            case RCF:
                return new RCF();
            case UFE:
                return new UFE();
            default:
                return null;
        }
    }
}
