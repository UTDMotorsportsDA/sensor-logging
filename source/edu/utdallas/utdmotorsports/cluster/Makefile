# Author: Michael Nelson
# Date: 4/6/2017
# Makefile: Makefile that produces ledScreenDisplays binary

CFLAGS =-Wall -O3 -g -std=c++11
OBJECTS=ledScreenDisplays.o
BINARIES=ledScreenDisplays
CXXFLAGS=$(CFLAGS)

RGB_LIB_DISTRIBUTION=..
RGB_INCDIR=$(RGB_LIB_DISTRIBUTION)/include
RGB_LIBDIR=$(RGB_LIB_DISTRIBUTION)/lib
RGB_LIBRARY_NAME=rgbmatrix
RGB_LIBRARY=$(RGB_LIBDIR)/lib$(RGB_LIBRARY_NAME).a
LDFLAGS=-L$(RGB_LIBDIR) -lrt -lm -lpthread -l$(RGB_LIBRARY_NAME)

all : $(BINARIES)

$(RGB_LIBRARY): FORCE
	$(MAKE) -C $(RGB_LIBDIR)

ledScreenDisplays: ledScreenDisplays.o $(RGB_LIBRARY)
	$(CXX) $(CXXFLAGS) $< -o $@ $(LDFLAGS)


%.o : %.cc
	$(CXX) -I$(RGB_INCDIR) $(CXXFLAGS) -c -o $@ $<

clean:
	rm -f $(OBJECTS) $(BINARIES)

FORCE:
.PHONY: FORCE
