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
import com.google.gson.GsonBuilder;

import fivium.pat.graphql.PAT_BaseQF;
import fivium.pat.utils.PAT_DAO;
import graphql.GraphQLException;
import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLObjectType;

public class GenerateSleepDataQF extends PAT_BaseQF {

  private static final String GENERATE_GRAPH_PREPARED_SQL_QUERY = "SELECT sleepdata.duration, sleepdata.efficiency, patient.first_name FROM sleepdata INNER JOIN patient ON sleepdata.study_id = patient.study_id WHERE date= (SELECT MAX(date) FROM sleepdata) ";
  private static final String GENERATE_USER_GRAPH_PREPARED_SQL_QUERY= "SELECT sleepdata.steps, sleepdata.efficiency, sleepdata.date, patient.first_name FROM sleepdata INNER JOIN patient ON sleepdata.study_id = patient.study_id WHERE sleepdata.study_id = ?";
  private static Log logger = LogFactory.getLog(GenerateSleepDataQF.class);

  @Override
  protected GraphQLObjectType defineField() {
    return newObject()
            .name("SleepData")
            .description("Get the latest sleep data for graphs")
            .field(newFieldDefinition()
  			    		.name("result")
  			            .type(GraphQLString))		    
  			   .build();	
  }

  @Override
  protected List<GraphQLArgument> defineArguments() {
  	return Arrays.asList(
  			new GraphQLArgument("study_id", Scalars.GraphQLString)
  			);
  }

  @Override
  protected Object fetchData(DataFetchingEnvironment environment) {
   
    Collection<Map<String, String>> sqlResult;
    Map<String, String> result = new HashMap<String, String>();
    Map<String, String> aMap = new HashMap<String, String>();
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    
    Object[] queryArgs = new Object[] {
				environment.getArgument("study_id")
			};

    try {
    	 if (queryArgs.length > 0 && queryArgs[0] != null ) {
    		 sqlResult = PAT_DAO.executeStatement(GENERATE_USER_GRAPH_PREPARED_SQL_QUERY, queryArgs);
       } else{
				sqlResult = PAT_DAO.executeStatement(GENERATE_GRAPH_PREPARED_SQL_QUERY, null);
       }
    	 if (!sqlResult.isEmpty()) {
					String dailySleepData = gson.toJson(sqlResult);
					aMap.put("dailySleepData", dailySleepData);
					String jsonResponse = gson.toJson(aMap);
					jsonResponse = jsonResponse.replace("\\", "");
					result.put("result", jsonResponse);
			}
    } catch (Exception e) {
    	logger.error("Unexpected error occured", e);
        throw new GraphQLException("Unexpected error fetching list of patients", e);
    }
    return result;
  }
}