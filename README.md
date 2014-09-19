# Neo4j Utilies



## Building

`sbt pack`

## Utilities

you can call each of this commands by `target/pack/bin/command`

### CompleteFile

param0: path to tsv file
param1: path to idiontology
add names to a tsv file containing mids in 3rd position


### exportRelationships

param0: path to outputfile
param1: path to idiontology
extracts all the type_rels from idiontology and dumps them in `outputfile`
relationships include relationships ids.

### LoadRelationshipsWeights

param0: File describing neo4j relationships weights
param1: path to idiontology

creates 'weight' property for the relationships described in the files with the given values

### MergeScoresAndRelationshipsIds 

param0: File describinng relationships(via mids) and weights
param1: File describing relationships(via mids) and relationships ids

###
