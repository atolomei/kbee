package test.com.novamens.kbee.content;


import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.Test;

import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.solr.indexer.service.SolrCoreClient;
import com.novamens.solr.indexer.service.SolrIndex;


public class SolrPartialUpdateTest {

	
	@Test
	public void run() {
		try {
		
			SolrCoreClient solrcore = new SolrCoreClient();
			solrcore.setUrl("http://localhost:8983/solr");
			solrcore.setName("windsorcontent");
			
			//SolrIndex index = new SolrIndex();
			//index.setServer(solrcore);
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.setField("id", "kbeeidoc#314046");
			Map<String,Object> fieldModifier = new HashMap<>(1);
			fieldModifier.put("set","true");
			doc.addField("head", fieldModifier);
			solrcore.add(doc);
			solrcore.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

}
