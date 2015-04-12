package org.test.gcd_java_protobuf;

import static com.google.api.services.datastore.client.DatastoreHelper.makeKey;
import static com.google.api.services.datastore.client.DatastoreHelper.makeOrder;
import static com.google.api.services.datastore.client.DatastoreHelper.makeProperty;
import static com.google.api.services.datastore.client.DatastoreHelper.makeValue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.datastore.DatastoreV1.CommitRequest;
import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.Mutation;
import com.google.api.services.datastore.DatastoreV1.MutationResult;
import com.google.api.services.datastore.DatastoreV1.PropertyOrder;
import com.google.api.services.datastore.DatastoreV1.Query;
import com.google.api.services.datastore.DatastoreV1.RunQueryRequest;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreHelper;

@RestController

public class Controller {

	private Datastore datastore;

	@RequestMapping("/entity")
	public void benchmark(HttpServletResponse response) throws IOException, DatastoreException {

		try {
			datastore = DatastoreHelper.getDatastoreFromEnv();
		} catch (GeneralSecurityException exception) {
			System.err.println("Security error connecting to the datastore: " + exception.getMessage());
			System.exit(2);
		} catch (IOException exception) {
			System.err.println("I/O error connecting to the datastore: " + exception.getMessage());
			System.exit(2);
		}

		response.setContentType("text/plain");

		List<Entity> employees1 = createEmployees(1);
		CommitRequest insertRequest1 = requestInsert(employees1);
		long startWriteMillis1 = System.currentTimeMillis();
		MutationResult insertResult1 = datastore.commit(insertRequest1).getMutationResult();
		long endWriteMillis1 = System.currentTimeMillis();
		response.getWriter().println("Write 1:" + (endWriteMillis1 - startWriteMillis1));

		RunQueryRequest query1 = requestEmployees();
		long startQueryMillis1 = System.currentTimeMillis();
		int countQuery1 = datastore.runQuery(query1).getBatch().getEntityResultList().size();
		long endQueryMillis1 = System.currentTimeMillis();
		response.getWriter().println("Query 1:" + (endQueryMillis1 - startQueryMillis1)
				+ " (Count: " + countQuery1 + ")");

		CommitRequest deleteRequest1 = requestDelete(insertResult1.getInsertAutoIdKeyList());
		long startDeleteMillis1 = System.currentTimeMillis();
		datastore.commit(deleteRequest1);
		long endDeleteMillis1 = System.currentTimeMillis();
		response.getWriter().println("Delete 1:" + (endDeleteMillis1 - startDeleteMillis1));

		//

		List<Entity> employees10 = createEmployees(10);
		CommitRequest insertRequest10 = requestInsert(employees10);
		long startWriteMillis10 = System.currentTimeMillis();
		MutationResult insertResult10 = datastore.commit(insertRequest10).getMutationResult();
		long endWriteMillis10 = System.currentTimeMillis();
		response.getWriter().println("Write 10:" + (endWriteMillis10 - startWriteMillis10));

		RunQueryRequest query10 = requestEmployees();
		long startQueryMillis10 = System.currentTimeMillis();
		int countQuery10 = datastore.runQuery(query10).getBatch().getEntityResultList().size();
		long endQueryMillis10 = System.currentTimeMillis();
		response.getWriter().println("Query 10:" + (endQueryMillis10 - startQueryMillis10)
				+ " (Count: " + countQuery10 + ")");

		CommitRequest deleteRequest10 = requestDelete(insertResult10.getInsertAutoIdKeyList());
		long startDeleteMillis10 = System.currentTimeMillis();
		datastore.commit(deleteRequest10);
		long endDeleteMillis10 = System.currentTimeMillis();
		response.getWriter().println("Delete 10:" + (endDeleteMillis10 - startDeleteMillis10));

		//

		List<Entity> employees100 = createEmployees(100);
		CommitRequest insertRequest100 = requestInsert(employees100);
		long startWriteMillis100 = System.currentTimeMillis();
		MutationResult insertResult100 = datastore.commit(insertRequest100).getMutationResult();
		long endWriteMillis100 = System.currentTimeMillis();
		response.getWriter().println("Write 100:" + (endWriteMillis100 - startWriteMillis100));

		RunQueryRequest query100 = requestEmployees();
		long startQueryMillis100 = System.currentTimeMillis();
		int countQuery100 = datastore.runQuery(query100).getBatch().getEntityResultList().size();
		long endQueryMillis100 = System.currentTimeMillis();
		response.getWriter().println("Query 100:" + (endQueryMillis100 - startQueryMillis100)
				+ " (Count: " + countQuery100 + ")");

		CommitRequest deleteRequest100 = requestDelete(insertResult100.getInsertAutoIdKeyList());
		long startDeleteMillis100 = System.currentTimeMillis();
		datastore.commit(deleteRequest100);
		long endDeleteMillis100 = System.currentTimeMillis();
		response.getWriter().println("Delete 100:" + (endDeleteMillis100 - startDeleteMillis100));
	}

	private List<Entity> createEmployees(int count) {
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < count; i++) {
			Entity.Builder entity = Entity.newBuilder();
			entity.setKey(makeKey("Employee"));
			entity.addProperty(makeProperty("firstName",
					makeValue(randomId())));
			entity.addProperty(makeProperty("lastName",
					makeValue(randomId())));
			Date hireDate = new Date();
			entity.addProperty(makeProperty("hireDate",
					makeValue(hireDate)));
			entities.add(entity.build());
		}
		return entities;
	}

	private CommitRequest requestInsert(List<Entity> entities) {
		Mutation.Builder mutation = Mutation.newBuilder()
				.addAllInsertAutoId(entities);
		CommitRequest req = CommitRequest.newBuilder()
				.setMutation(mutation)
				.setMode(CommitRequest.Mode.NON_TRANSACTIONAL)
				.build();
		return req;
	}

	private CommitRequest requestDelete(List<Key> keys) {
		Mutation.Builder mutation = Mutation.newBuilder()
				.addAllDelete(keys);
		CommitRequest req = CommitRequest.newBuilder()
				.setMutation(mutation)
				.setMode(CommitRequest.Mode.NON_TRANSACTIONAL)
				.build();
		return req;
	}
	
	private RunQueryRequest requestEmployees() {
		Query.Builder query = Query.newBuilder();
		query.addKindBuilder().setName("Employee");
		query.addOrder(makeOrder("hireDate", PropertyOrder.Direction.DESCENDING));
		RunQueryRequest.Builder request = RunQueryRequest.newBuilder();
		request.setQuery(query.build());
		return request.build();
	}

	private String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

	private java.util.Random rand = new java.util.Random();

	private String randomId() {
		StringBuilder builder = new StringBuilder();
		while(builder.toString().length() == 0) {
			int length = rand.nextInt(5)+5;
			for(int i = 0; i < length; i++)
				builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
		}
		return builder.toString();
	}

}
