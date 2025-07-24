FROM docker.elastic.co/elasticsearch/elasticsearch:8.13.4

RUN set -eux; \
    for plugin in analysis-nori analysis-kuromoji analysis-icu; do \
        if ! bin/elasticsearch-plugin list | grep -q "$plugin"; then \
            echo "→ Installing $plugin"; \
            bin/elasticsearch-plugin install --batch "$plugin"; \
        else \
            echo "→ $plugin already installed, skipping"; \
        fi; \
    done
