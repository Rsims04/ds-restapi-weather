# Location of trees:
SOURCE_DIR := src/build
SOURCE_TEST_DIR := src/test
OUTPUT_DIR := dest

# Unix tools
AWK := awk
FIND := find
MKDIR := mkdir -p
RM := rm -rf
SHELL := /bin/bash

# Set the Java classpath
class_path := OUTPUT_DIR

# Java tools
JC := javac
JAVA := java
JFLAGS := -sourcepath $(SOURCE_DIR) \
-d $(OUTPUT_DIR)
JVMFLAGS := -cp $(OUTPUT_DIR)
TESTFLAGS := -cp lib/junit-4.13.2.jar
JVM := $(JAVA) $(JVMFLAGS)

# Set the java CLASSPATH
class_path := OUTPUT_DIR

# make-directories - Ensure output directory exists.
make-directories := $(shell $(MKDIR) $(OUTPUT_DIR))

# all - Perform all tasks for a complete build
.PHONY: compile
all: compile

all_javas := $(OUTPUT_DIR)/all.javas
all_tests := $(OUTPUT_DIR)/all.tests

# compile - Compile the source
.PHONY: compile
compile: $(all_javas)
	$(JC) $(JFLAGS) @$<

# all_javas - Gather source file list
.INTERMEDIATE: $(all_javas)
$(all_javas):
	$(FIND) $(SOURCE_DIR) -name '*.java' > $@

.INTERMEDIATE: $(all_tests)
$(all_tests):
	$(FIND) $(SOURCE_TEST_DIR) -name '*Test.java' > $@

TEST_SOURCE := test.AggregationServerTest \
test.AggregationServerThreadTest \
test.ContentServerTest \
test.GETClientTest \
test.JSONParserTest \
test.LamportClockTest \
test.LocalStorageTest \

.PHONY: test
test:
	$(JC) $(TESTFLAGS) $(SOURCE_TEST_DIR)/*.java src/build/*.java -d dest 
	java -cp dest:lib/hamcrest-core-1.3.jar:lib/junit-4.13.2.jar org.junit.runner.JUnitCore $(TEST_SOURCE)


.PHONY: clean
clean:
	$(RM) $(OUTPUT_DIR)
	rm localStorage.txt
	rm clock

.PHONY: classpath
classpath:
	@echo CLASSPATH='$(CLASSPATH)'

.PHONY: print
print:
	$(foreach v, $(V), \
		$(warning $v = $($v)))

.PHONY: client
client:
	$(JVM) build.GETClient

.PHONY: server
server:
	$(JVM) build.AggregationServer

.PHONY: content
content:
	$(JVM) build.ContentServer


