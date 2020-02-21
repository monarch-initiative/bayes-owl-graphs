[![Build Status](https://travis-ci.org/monarch-initiative/kboom.svg?branch=master)](https://travis-ci.org/monarch-initiative/kboom)

## BOOM: Bayes OWL Ontology Merging

BOOM is an ontology construction technique that combines deductive reasoning and probabilistic inference. It takes two or more ontologies linked by hypothetical axioms, and estimates the most likely unified logical ontology.

## k-BOOM

k-BOOM is a version of BOOM that works by factorizing the probabilistic ontology into k submodules. It was used to build the [Mondo](http://obofoundry.org/ontology/mondo.html) merged disease ontology.

For more information see:

 * Preprint: Mungall CJ, Koehler S, Robinson P, Holmes I, Haendel M. k-BOOM: A Bayesian approach to ontology structure inference, with applications in disease ontology construction. bioRxiv. 2019. https://www.biorxiv.org/content/10.1101/048843v3
 * [PhenoDay presentation](http://f1000research.com/slides/5-1648)

To run:

```
mvn install
bin/kboom -t all-ptable.tsv all.owl -o merged.owl
```
