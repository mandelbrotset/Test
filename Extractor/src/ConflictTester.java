import static org.junit.Assert.*;

import org.junit.Test;

public class ConflictTester {

	@Test
	public void test() {
		//fail("Not yet implemented");
		
		String conflict = "Conflict type: SameSignatureCM"+"\n"
+"Merge Commit SHA-1: eed557742f5db7cd82d22b69a84220cc598fd744"+"\n"
+"Parent1 SHA-1: 04681ef6165a40192a6e268ca2df861d9d017f47"+"\n"
+"Parent2 SHA-1: bbeb09eae7ac3c5d9837bb26eacfac6bba468929"+"\n"
+"Number of Conflicts: 1"+"\n"
+"Different Spacing: 0"+"\n"
+"Consecutive Lines: 0"+"\n"
+"Intersection: 0"+"\n"
+"Cause same signature: renamedMethod"+"\n"
+"Possible renaming: 0"+"\n"
+"Conflict body:"+"\n" 
+"<<<<<<< /tmp/paola/paolaboarba/elasticsearch/fstmerge_tmp1462368288471/fstmerge_var1_6390924134105912809"+"\n"
+"@Override"+"\n"
+"    public SuggestionSearchContext.SuggestionContext parse(XContentParser parser, QueryShardContext shardContext) throws IOException {"+"\n"
+"        MapperService mapperService = shardContext.getMapperService();"+"\n"
+"        XContentParser.Token token;"+"\n"
+"        String fieldName = null;"+"\n"
+"        TermSuggestionContext suggestion = new TermSuggestionContext(suggester);"+"\n"
+"        DirectSpellcheckerSettings settings = suggestion.getDirectSpellCheckerSettings();"+"\n"
+"        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {"+"\n"
+"            if (token == XContentParser.Token.FIELD_NAME) {"+"\n"
+"                fieldName = parser.currentName();"+"\n"
+"            } else if (token.isValue()) {"+"\n"
+"                parseTokenValue(parser, mapperService, fieldName, suggestion, settings, mapperService.getIndexSettings().getParseFieldMatcher());"+"\n"
+"            } else {"+"\n"
+"                throw new IllegalArgumentException(\"suggester[term]  doesn't support field [\" + fieldName + \"]\");"+"\n"
+"            }"+"\n"
+"        }"+"\n"
+"        return suggestion;"+"\n"
+"    }"+"\n"
+"||||||| /tmp/paola/paolaboarba/elasticsearch/fstmerge_tmp1462368288471/fstmerge_base_5647615206938341563"+"\n"
+"======="+"\n"
+"@Override"+"\n"
+"    public SuggestionSearchContext.SuggestionContext parse(XContentParser parser, QueryShardContext shardContext) throws IOException {"+"\n"
+"        MapperService mapperService = shardContext.getMapperService();"+"\n"
+"        XContentParser.Token token;"+"\n"
+"        String fieldName = null;"+"\n"
+"        TermSuggestionContext suggestion = new TermSuggestionContext(shardContext);"+"\n"
+"        DirectSpellcheckerSettings settings = suggestion.getDirectSpellCheckerSettings();"+"\n"
+"        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {"+"\n"
+"            if (token == XContentParser.Token.FIELD_NAME) {"+"\n"
+"                fieldName = parser.currentName();"+"\n"
+"            } else if (token.isValue()) {"+"\n"
+"                parseTokenValue(parser, mapperService, fieldName, suggestion, settings, mapperService.getIndexSettings().getParseFieldMatcher());"+"\n"
+"            } else {"+"\n"
+"                throw new IllegalArgumentException(\"suggester[term]  doesn't support field [\" + fieldName + \"]\");"+"\n"
+"            }"+"\n"
+"        return suggestion;"+"\n"
+"    }"+"\n"
+">>>>>>> /tmp/paola/paolaboarba/elasticsearch/fstmerge_tmp1462368288471/fstmerge_var2_2444428870676219269"+"\n"
+""+"\n"
+"File path: /tmp/downloads/elasticsearch/revisions/rev_04681_bbeb0/rev_04681-bbeb0/core/src/main/java/org/elasticsearch/search/suggest/term/TermSuggestParser.java";		
		Conflict c = new Conflict(conflict);
						
	}
}