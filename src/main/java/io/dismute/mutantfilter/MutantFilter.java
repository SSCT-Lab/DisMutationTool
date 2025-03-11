package io.dismute.mutantfilter;

import io.dismute.mutantgen.Mutant;

import java.util.List;

public interface MutantFilter {
    public List<Mutant> filter(List<Mutant> mutants);
}
