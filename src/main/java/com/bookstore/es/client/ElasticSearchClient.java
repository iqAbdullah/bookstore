package com.bookstore.es.client;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bookstore.es.model.ESEntity;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.Delete;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

@Component
public class ElasticSearchClient {

	private static final String BODY = "body";
	private static final String TITLE = "title";

	@Value("${media.index}")
	private String esIndex;

	@Value("${media.type}")
	private String esType;

	@Autowired
	private JestClient jestClient;

	@Value("${elastic.search.config.endpoint}")
	private String host;

	public <T extends ESEntity> boolean insertBulk(List<T> records)
			throws IOException {

		final List<Index> indexList = records.stream().map(this::getIndexQuery).collect(Collectors.toList());

		JestResult jestResult = jestClient
				.execute(new Bulk.Builder().defaultIndex(esIndex).defaultType(esType).addAction(indexList).build());
		return jestResult.isSucceeded();
	}

	private Index getIndexQuery(ESEntity esEntity) {
		return new Index.Builder(esEntity).id(esEntity.getId()).build();
	}

	public List<String> orSearchQuery(final String value) throws IOException {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder query = QueryBuilders.boolQuery().should(QueryBuilders.termQuery(TITLE, value))
				.should(QueryBuilders.termQuery(BODY, value));
		searchSourceBuilder.query(query);
		List<String> resultlist = jestClient
				.execute(new Search.Builder(searchSourceBuilder.toString()).addIndex(esIndex).addType(esType).build())
				.getSourceAsStringList();
		return resultlist;
	}

	public boolean delete(final String id) throws IOException {
		JestResult jestResult = jestClient.execute(new Delete.Builder(id).index(esIndex).type(esType).build());
		return jestResult.isSucceeded();
	}
	
	public String get(final String id) throws IOException {
		JestResult jestResult = jestClient.execute(new Get.Builder(esIndex, id).index(esIndex).type(esType).build());
	   return jestResult.getSourceAsString();
	}

}
