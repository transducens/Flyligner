#! /bin/bash

DIR=$(readlink -f $(dirname $0))
echo $DIR

java -cp $DIR/../dist/Flyligner.jar es.ua.dlsi.alignment.AlignmentWithoutTraining -s $DIR/es.txt -t $DIR/en.txt --source-translations $DIR/es.segs --target-translations $DIR/en.segs -m 3 -o $DIR/no_training.output --symmetrisation gdfa

java -cp $DIR/../dist/Flyligner.jar es.ua.dlsi.alignment.TrainedAlignment -s $DIR/es.txt -t $DIR/en.txt --source-translations $DIR/es.segs --target-translations $DIR/en.segs -w $DIR/weights.l3 -o $DIR/trained.output --symmetrisation gdfa
