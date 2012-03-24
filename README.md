# README

BSAX-J is a library of Java Classes for transcoding from SAX events to BSAX streams and back.

There has been much discussion (controversy, even) over the last few years about possibly binary encodings for XML. While I have not yet come to a final conclusion about the need for a binary XML format, BSAX is my idea of one possible encoding that leverages other XML prior art (SAX events and UTF-8, in particular).

## STATUS

The 0.8.0 version of BSAX-J is complete in that it can be used to perform round-trip converstions from textual XML to SAX events to BSAX binary streams, and back to SAX events and textual XML. Indeed, the test code in the distribution does exaxtly that for a simple example XML file and measures the difference in file size (*the file is slightly smaller for the BSAX encoding of the sample file*) and the difference in read time (*the read time is significantly faster for the sample file*).

The streaming encoder included in the 0.8 BSAX-J distribution is very simple, and that is on purpose. There are use cases that would cause it to be significantly sub-optimal (those with high cardinality of unique string string set vs. number of SAX events). Other encoders are possible that would do better for such use cases, without changing the BSAX format specification.

Before a 1.0.0 version of BSAX-J is released, there is one more important feature that will be included in the specification, and that is an operation for referencing a "bootstrapping" string table that can be mutually agreed upon by the encoder and the decoder. Such a string table may be based on a DTD or XML Schema or other source of candidate strings. This will allow the repeated communications using similar vocabularies to achieve additional speedups and space savings. The operation will include arguments for one or two ways of identifying the desired bootstap string table by name, and also some sort of fingerprint (possibly an MD5 sum) so the consumer can ensure that it only proceeds if its local copy of the bootstrap string table matches the one originally used by the producer when encoding the BSAX stream.

Once BSAX-J reaches version 1.0, we hope to have encoders and decoders for other languages such as Perl, Python and Ruby. Contributions on BSAX-J itself or other implementations would be greatly appreciated. You can contact the author (Gregor N. Purdy, Sr.) at <gnp@acm.org>.

