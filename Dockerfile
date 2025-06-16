FROM docker.elastic.co/elasticsearch/elasticsearch:8.13.4
RUN elasticsearch-plugin install --batch analysis-nori     \
 && elasticsearch-plugin install --batch analysis-kuromoji \
 && elasticsearch-plugin install --batch analysis-icu