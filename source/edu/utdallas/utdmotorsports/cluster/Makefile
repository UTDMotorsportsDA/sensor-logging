# Author: Michael Nelson
# Date: 4/6/2017
# Makefile: Makefile that produces ledDisplayFunctions shared library

CFLAGS =-Wall -O3 -g -std=c++11
CXXFLAGS=$(CFLAGS)

RGB_LIB_DISTRIBUTION=..
RGB_INCDIR=$(RGB_LIB_DISTRIBUTION)/include
RGB_LIBDIR=$(RGB_LIB_DISTRIBUTION)/lib
RGB_LIBRARY_NAME=rgbmatrix
RGB_LIBRARY=$(RGB_LIBDIR)/lib$(RGB_LIBRARY_NAME).a
LDFLAGS=-L$(RGB_LIBDIR) -lrt -lm -lpthread -l$(RGB_LIBRARY_NAME)
JAVA_PATH=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include

all : libLedDisplayFunctions.so

libLedDisplayFunctions.so: ledDisplayFunctions.cc $(RGB_LIBRARY)
	$(CXX) $(CXXFLAGS) -shared -fPIC -I$(RGB_INCDIR) -I$(JAVA_PATH) -I$(JAVA_PATH)/linux $< -o $@ $(LDFLAGS)

$(RGB_LIBRARY): FORCE
	$(MAKE) -C $(RGB_LIBDIR)

copy:
	cp libLedDisplayFunctions.so /usr/lib

clean:
	rm -f libLedDisplayFunctions.so main.class

FORCE:
.PHONY: FORCE
