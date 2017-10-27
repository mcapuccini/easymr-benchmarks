#!/bin/bash

docker run -v $PWD:/host --name docking_full mcapuccini/oe-docking \
  fred -receptor /var/openeye/hiv1_protease.oeb \
  -hitlist_size 0 \
  -conftest none \
  -dbase /host/aksci_p0.0.sdf /host/aksci_p1.0.sdf \
  -docked_molecule_file /host/output.sdf

docker run -v $PWD:/host --name sorting_full mcapuccini/sdsorter \
  sdsorter -reversesort='FRED Chemgauss4 score' \
  -keep-tag='FRED Chemgauss4 score' \
  -nbest=3 \
  /host/output.sdf /host/serial_results.sdf

