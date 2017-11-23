#!/bin/csh -f
# usual.sdf.csh from http://zinc.docking.org
# Run this script to download ZINC
# Requires curl (default) or wget, http://wget.docking.org
#
# Thus, to run this script
#         using curl, do:     csh usual.sdf.csh
#         using wget, do:     csh usual.sdf.csh wget
#
setenv base http://zinc.docking.org/db/byvendor/aksci
setenv fn .zinc.$$
cat <<+ > $fn
aksci_p0.0.sdf.gz
aksci_p1.0.sdf.gz
+
if ($#argv>0) then
     wget --base=$base -i < $fn
else
     foreach i (`cat $fn`)
          curl --url $base/$i -o $i
     end
endif
rm -f $fn
# File created on  Sat Jul 25 12:54:32 PDT 2015
# This is the end of the csh script.
