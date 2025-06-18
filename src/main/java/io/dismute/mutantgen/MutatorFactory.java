package io.dismute.mutantgen;


import io.dismute.mutator.MutatorBase;
import io.dismute.mutator.concurrency.*;
import io.dismute.mutator.consistency.*;
import io.dismute.mutator.network.*;
import io.dismute.mutator.deprecated.*;

public class MutatorFactory {
    public static MutatorBase getMutator(MutatorType type) {
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
