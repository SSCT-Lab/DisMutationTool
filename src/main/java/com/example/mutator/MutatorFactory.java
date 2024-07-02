package com.example.mutator;

import com.example.mutator.deprecated.*;
import com.example.mutator.network.*;
import com.example.mutator.concurrency.*;
import com.example.mutator.consistency.*;

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
            case RNE:
                return new RNE();
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
            case RCE:
                return new RCE();
            // consistency
            case RCF:
                return new RCF();
            case UFE:
                return new UFE();
            case RFE:
                return new RFE();
            // traditional

            default:
                return null;
        }
    }
}
