#!/usr/bin/env bash

java -cp out -Djava.library.path=JNI/lib fsae.da.car.CarMain $1 $2 $3 $4 $5
bash