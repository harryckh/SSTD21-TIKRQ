
# SSTD21-TIKRQ

Notes
=======================

1. This code was used for the empirical study of the SSTD 2021 paper "Time-Constrained Indoor Keyword-aware Routing".
2. This code is developed by Harry Kai-Ho Chan (kai-ho@ruc.dk) and Tiantian Liu (liutt@cs.aau.dk).
3. This code is written in Java.
4. This code is tested on the macOS 10.15.7 environment.

Usage
=======================
**Step 1 [Partition Keyword Assignment]**   
(A sample assignment is included. You can skip this step if you do not want to change any parameters.)

1. To assign keywords to partitions, execute `datagenerate/AssignWords.java`. It reads *words/ciword\_i.txt* (the manual categorized i-words)
and *words/wordRelationship\_identity.txt*
and generates *words/partition\_words.txt*


2. To generate the keyword mapping, execute `wordProcess/WordPro.java`. It reads *words/ciword\_c.txt* and *words/partition\_word.txt* (generated above),
to generate and stores other keyword mappings files .

3. To generate Static Cost and Waiting time of partitions, execute `datagenerate/AssignWtimeCost.java`. It generates *info\_cost\_wtime/info\_cost\_wtime.txt*. 
A sample file is included.

The format of all generated files can be found in Appendix B.

**Step 2. [Query Execution]** 

1. To generate the query set, execute `experiment/QueryGen.java`. It generates the query set that are to be executed in the experiment. Each generated query has at least one returned as the result. A sample query set *query/default.txt* is included.

2. To run the experiment, execute `experiment/ExpTIKRQ.java`. The parameter explantion can be found in Appendix A. The corresponding result.txt and stat.txt will be generated.
A sample result file *defaultSSAresult.txt* and a sample statistics file *defaultSSAstat.txt* are included.

**Step 3. [Result Collection]** 

Collect the querying results and running statistics (you can ignore this step if you don't want to collect the information of querying results and running statistics). The statistics file format is explained in Appendix D. 

Appendix A. Parameter
============================

    <alg_opt>
      =		1: Set-based Search Algorithm (SSA)
      =		2: Adapted KoE 
      =		3: SSA without Pruning

Appendix B. Files Format
============================
   
File |  Description | Format
------------- | ------------- | -------------
words/ciword_c.txt	 |	Mapping from c-word to i-words | c-word \t i-word[0], i-word[1],...
words/ciword_i.txt	 |	Mapping from i-word to c-word | i-word \t c-word
words/par\_cword\_c.txt	|	Mapping from c-word to parID | c-word \t par0 \t par1 \t ...
words/par\_cword\_p.txt	|	Mapping from parID to c-word | parID \t c-word
words/par\_iword\_i.txt	|	Mapping from i-word to parID | i-word \t par0 \t par1 \t ...
words/par\_iword\_p.txt	|	Mapping from parID to i-word | parID \t i-word
words/par\_tword\_p.txt	|	Mapping from t-word to parID | t-word \t par0 \t par1 \t ...
words/par\_tword\_t.txt	|	Mapping from parID to t-words | parID \t t-word[0] \t t-word[1] \t ...
info\_cost\_wtime/info\_cost\_wtime.txt | Waiting time and static cost of each partition | parID \t cost \t waitTime \n

Appendix C. Query File Format
============================
Each row in query file has the following format.

	<Ps X-coord> <Ps Y-coord> <Ps floor> <Pt X-coord> <Pt Y-coord> <Pt floor> <number of query keywords> <1st query keyword> <2nd query keyword> ... <last query keyword> <Delta_{Max}> <relevance threshold> <k>


Appendix D. Statistic File Format
============================

	row	time(ns)	memory
	1		<average execution time in ns>	<average memory usage>
	2		...		...
	...		...		...			

where each row corresponds to the average of one query file.


