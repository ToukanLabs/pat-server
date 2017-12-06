package fivium.pat.graphql.queryfields.clinician;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

import fivium.pat.graphql.PAT_BaseQF;
import fivium.pat.utils.PAT_DAO;
import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLObjectType;

public class GetGraphDataQF extends PAT_BaseQF {

	private static final String GET_GRAPH_PREPARED_SQL_QUERY = "SELECT s.steps, p.first_name FROM stepdata s INNER JOIN patient_details p ON s.study_id = p.study_id WHERE s.date= (SELECT MAX(s.date) FROM stepdata) ";
	private static final String GET_USER_GRAPH_PREPARED_SQL_QUERY = "SELECT s.steps, s.date, p.first_name FROM stepdata s INNER JOIN patient_details p ON s.study_id = p.study_id WHERE s.study_id = ?";
	private static Log logger = LogFactory.getLog(GetGraphDataQF.class);

	@Override
	protected GraphQLObjectType defineField() {
		return newObject().name("GetGraphData").description("Get the latest step data for graphs")
				.field(newFieldDefinition().name("result").type(GraphQLString)).build();
	}

	@Override
	protected List<GraphQLArgument> defineArguments() {
		return Arrays.asList(new GraphQLArgument("study_id", Scalars.GraphQLString));
	}

	@Override
	protected Object fetchData(DataFetchingEnvironment environment) {

		Map<String, String> result = new HashMap<String, String>();

		Object[] queryArgs = new Object[] { environment.getArgument("study_id") };

		try {
			
			Collection<Map<String, String>> sqlResult;
			if (queryArgs.length > 0 && queryArgs[0] != null) {
				sqlResult = PAT_DAO.executeStatement(GET_USER_GRAPH_PREPARED_SQL_QUERY, queryArgs);
			} else {
				sqlResult = PAT_DAO.executeStatement(GET_GRAPH_PREPARED_SQL_QUERY, null);
			}
			
			if (sqlResult.isEmpty()) {
				result.put("result", "No data returned.");
				return result;
			}
			
			result.put("result", new Gson().toJson(sqlResult));
		} catch (Exception e) {
			logger.error("Unexpected error occured", e);
			result.put("result", "Unexpected error fetching list of patients");
		}
		return result;
	}
}