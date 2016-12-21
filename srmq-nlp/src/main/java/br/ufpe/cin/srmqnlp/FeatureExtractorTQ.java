package br.ufpe.cin.srmqnlp;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import edu.stanford.nlp.ie.pascal.TeXHyphenator;
import edu.stanford.nlp.ling.CoreAnnotations.AfterAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.BeforeAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

public class FeatureExtractorTQ {
	
	private static final String urlRegEx = "/<?(https?|ftp|file):\\/\\/[-a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#\\/%=~_|]>?/";
	TokenSequencePattern patternForURLs;

	public FeatureExtractorTQ() {
		this.patternForURLs = TokenSequencePattern.compile(urlRegEx);
		
	}
	
	public static class RelationalVisualFeatures {
		private int auxVerbCount;
		@Override
		public String toString() {
			return "Count of auxiliary verbs: " + auxVerbCount;
		}
	}
	
	public static class TokenVisualFeatures {
		private int pronounCount;
		private int conjCount;
		private int prepCount;
		private int verbToBeCount;
		private int punctuationCount;
		
		private int minQuotedTextLength;
		private int numberQuotedTexts;
		private int _totalLengthQuotedText;
		private int _currentQuotedTextLength;
		private int _inquotedText;
		private int maxLengthQuotedText;
		
		private int numberOfSentences;
		private int capitalizedWords;
		private int numberOfCharacters;
		
		private int whitespaceViolations;
		private int _currentSentenceIndex;
		
		private int numberOfURLs;
		
		private int numberOfWords;
		
		private int capitalizationViolations;
		
		private boolean _lastOneWasPunctuation;
		private int punctuationViolations;
		
		private int numberQuestionMarks;
		
		private int numberWhiteSpaces;
		
		private int _capitalLettersCharacters;

		@Override
		public String toString() {
			return "Count of pronouns: " + pronounCount
					+ "\nCount of conjunctions: " + conjCount
					+ "\nCount of prepositions: " + prepCount
					+ "\nCount of occurences of the verb \"to be\": " + verbToBeCount
				    + "\nCount of punctuation marks: " + punctuationCount
				    + "\nMinimum length of quoted text: " + minQuotedTextLength
				    + "\nAverage length of quoted text: " + ((numberQuotedTexts > 0) ? (_totalLengthQuotedText/(double)numberQuotedTexts) : "0")
				    + "\nMaximum length of quoted text: " + maxLengthQuotedText
				    + "\nNumber of quotes: " + numberQuotedTexts
					+ "\nNumber of sentences: " + numberOfSentences
					+ "\nNumber of capitalized words: " + capitalizedWords
					+ "\nNumber of characters: " + numberOfCharacters
					+ "\nNumber of whitespace violations: " + whitespaceViolations
					+ "\nNumber of URLs: " + numberOfURLs
				    + "\nNumber of words: " + numberOfWords
				    + "\nNumber of capitalization violations: " + capitalizationViolations
				    + "\nNumber of question marks: " + numberQuestionMarks
				    + "\nNumber of punctuation violations: " + punctuationViolations
				    + "\nNumber of whitespaces: " + numberWhiteSpaces
				    + "\nPunctuation characters divided by all characters: " + ((numberOfCharacters > 0) ? (punctuationCount/(double)numberOfCharacters) : "0") 
				    + "\nWhitespace characters divided by all characters: " + ((numberOfCharacters > 0) ? (numberWhiteSpaces/(double)numberOfCharacters) : "0")
				    + "\nCapital letters characters divided by all characters: " + ((numberOfCharacters > 0) ? (_capitalLettersCharacters/(double)numberOfCharacters) : "0");
			
		}
		
	}

	private void addRelationalVisualFeatures(CoreMap sentence, RelationalVisualFeatures features) {
        SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);
        for (SemanticGraphEdge e : dependencies.edgeIterable()) {
            if (e.getRelation().toString().equalsIgnoreCase("aux")) {
            	features.auxVerbCount++;
            }
        }
	}
	
	private boolean isPronoun(String pos) {
		return (pos.startsWith("WP") || pos.equals("PRP") || pos.startsWith("PP"));
	}
	
	private boolean isConjunction(String pos) {
		return (pos.equals("CC"));
	}
	
	private boolean isPreposition(String pos) {
		return (pos.equals("IN"));
	}
	
	private boolean isVerb(String pos) {
		return (pos.startsWith("VB"));
	}
	
	private int countCharsLike(char c, String str) {
		int result = 0;
		int from = str.indexOf(c);
		while(from != -1) {
			result++;
			from++;
			from = str.indexOf(c, from);
		}
		return result;
	}
	
	private int countCapitalLetters(String str) {
		int capitalLetters = 0;
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i))) {
				capitalLetters++;
			}
		}
		return capitalLetters;
	}
	
	private boolean isToBeForm(String word) {
		final String wordLower = word.toLowerCase(Locale.ENGLISH);
		return (wordLower.equals("was") || wordLower.equals("were") || wordLower.equals("been")
				|| wordLower.equals("being") || wordLower.equals("am") || wordLower.equals("are")
				|| wordLower.equals("is") || wordLower.equals("be"));
	}
	
	private boolean isPunctuationMark(String pos) {
		return (pos.equals(".") || pos.equals(",") || pos.equals(":"));
	}
	
	private boolean isGroupingToken(String pos) {
		return (pos.equals("-LRB-") || pos.equals("-RRB-"));
	}
	
	private boolean posCountsAsWord(String pos) {
		return (!isPunctuationMark(pos) && !isGroupingToken(pos) && !isQuote(pos));
	}
	
	private boolean isQuote(String pos) {
		return (pos.equals("``") || (pos.equals("''")));
	}
	
	private boolean isOpeningQuote(String pos, TokenVisualFeatures features) {
		return(pos.equals("``") || (pos.equals("''") && (features._inquotedText == 0)));
	}
	
	private boolean isClosingQuote(String pos, TokenVisualFeatures features) {
		return ((features._inquotedText > 0) && pos.equals("''"));
	}
	
	private void addTokenVisualFeatures(CoreMap sentence, TokenVisualFeatures features) {
		List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
		int numTokens = tokens.size();
		computeNumberOfURLs(features, tokens);
        for (CoreLabel token: tokens) {
        	//token indices are 1-based
        	final int tokenIndex = token.get(IndexAnnotation.class);
            // this is the POS tag of the token
            String pos = token.get(PartOfSpeechAnnotation.class);
            
            if (posCountsAsWord(pos)) {
            	features.numberOfWords++;
            }
            
        	// this is the text of the token
            String word = token.get(TextAnnotation.class);
            
            final int numberOfQuestionMarks = countCharsLike('?', word);
            features.numberQuestionMarks += numberOfQuestionMarks;
            
            final int capitalLetters = countCapitalLetters(word);
            features._capitalLettersCharacters += capitalLetters;
            
            if (features._inquotedText > 0) {
            	features._currentQuotedTextLength += word.length();
            }
            
            final char firstChar = word.charAt(0);
            if (Character.isUpperCase(firstChar)) {
            	features.capitalizedWords++;
            } else {
            	if (tokenIndex == 1 && Character.isLowerCase(firstChar)) {
            		features.capitalizationViolations++;
            	}
            }
            
        	String spaceBefore = token.get(BeforeAnnotation.class);
        	features.numberWhiteSpaces += spaceBefore.length();
    		if (features._currentSentenceIndex == (features.numberOfSentences - 1)) {
    			//last sentence, count space after last token
    			if (tokenIndex == numTokens) {
    				String spaceAfter = token.get(AfterAnnotation.class);
    				features.numberWhiteSpaces += spaceAfter.length();
    			}
    		}
        	if (spaceBefore.length() > 1)
        		features.whitespaceViolations++;
            
 
            if (isPronoun(pos)) {
            	features.pronounCount++;
            } else if (isConjunction(pos)) {
            	features.conjCount++;
            } else if (isPreposition(pos)) {
            	features.prepCount++;
            } else if (isVerb(pos) && isToBeForm(word)) {
            	features.verbToBeCount++;
            } else if (isPunctuationMark(pos)) {
            	features.punctuationCount++;
            	//bogus space before punctuation is counted as whitespace violation
            	if (spaceBefore.length() == 1)
            		features.whitespaceViolations++;
            	//Missing space after punctuation is counted as whitespace violation, unless it is the last token of the last sentence
            	String spaceAfter = token.get(AfterAnnotation.class);
            	if(spaceAfter.length() == 0) {
            		if (features._currentSentenceIndex != (features.numberOfSentences - 1)) {
            			features.whitespaceViolations++;
            		} else {
            			//this is the last sentence, check if it is the last token (token indices are 1-based)
            			if (tokenIndex != numTokens) {
            				features.whitespaceViolations++;
            			}
            		}
            	}
            	if (features._lastOneWasPunctuation) {
            		features.punctuationViolations++;
            	}
            } else if(isOpeningQuote(pos, features)) {
            	features._inquotedText++;
            	features._currentQuotedTextLength = 0;
            } else if(isClosingQuote(pos, features)) {
            	features.numberQuotedTexts++;
            	features._inquotedText--;
            	features._currentQuotedTextLength -= word.length();
            	features._totalLengthQuotedText += features._currentQuotedTextLength;
            	if (features._currentQuotedTextLength > features.maxLengthQuotedText) {
            		features.maxLengthQuotedText = features._currentQuotedTextLength;
            	}
            	if (features.minQuotedTextLength == 0 || (features._currentQuotedTextLength < features.minQuotedTextLength)) {
            		features.minQuotedTextLength = features._currentQuotedTextLength;
            	}
            }
            
            if (tokenIndex == tokens.size()) {
            	if (!isPunctuationMark(pos)) {
            		features.punctuationViolations++;
            	}
            }
            
            if (!isPunctuationMark(pos)) {
            	features._lastOneWasPunctuation = false;
            } else {
            	features._lastOneWasPunctuation = true;
            }
        }
	}

	private void computeNumberOfURLs(TokenVisualFeatures features, List<CoreLabel> tokens) {
		TokenSequenceMatcher matcher = patternForURLs.getMatcher(tokens);
		while (matcher.find()) {
			// System.out.println(">>>MATCHED: " + matcher.group());
			features.numberOfURLs++;
		}
	}
	
	public static void main(String[] args) {
		FeatureExtractorTQ featureExtractor = new FeatureExtractorTQ();
		Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        TeXHyphenator teXHyphenator = new TeXHyphenator();
        teXHyphenator.loadDefault();

        String text = "i was  dancing the polka ,do you dance the polka ? I [do] think so. please visit https://www.facebook.com/sergio.queiroz and http://stackoverflow.com/questions/163360/regular-expression-to-match-urls-in-java. Hello? Are you there?"; // Add your text here!
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        RelationalVisualFeatures rvFeatures = new RelationalVisualFeatures();
        TokenVisualFeatures tvFeatures = new TokenVisualFeatures();
        
        tvFeatures.numberOfSentences = sentences.size();
        tvFeatures.numberOfCharacters = document.get(TextAnnotation.class).length();
        for(CoreMap sentence: sentences) {
            SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);
            //IndexedWord root = dependencies.getFirstRoot();
            //System.out.printf("root(ROOT-0, %s-%d)%n", root.word(), root.index());
            for (SemanticGraphEdge e : dependencies.edgeIterable()) {
                System.out.printf ("%s(%s-%d, %s-%d)%n", e.getRelation().toString(), e.getGovernor().word(), e.getGovernor().index(), e.getDependent().word(), e.getDependent().index());
            }

            tvFeatures._currentSentenceIndex = sentence.get(SentenceIndexAnnotation.class); 
        	featureExtractor.addRelationalVisualFeatures(sentence, rvFeatures);
        	featureExtractor.addTokenVisualFeatures(sentence, tvFeatures);

            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
            	// this is the text of the token
                String word = token.get(TextAnnotation.class);
                int index = token.get(IndexAnnotation.class);
                boolean [] breakPoints = teXHyphenator.findBreakPoints(word.toLowerCase(Locale.ENGLISH).toCharArray());
                
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
                System.out.print("[" + tvFeatures._currentSentenceIndex + ", " + index + "]:(" + word + ", " + pos + ", " + ne + ") ");
                for (int i = 0; i < word.length(); i++) {
                	if (breakPoints[i]) 
                		System.out.print('-');
                	System.out.print(word.charAt(i));
                }
                System.out.println(" Before: \"" + token.get(BeforeAnnotation.class) + "\" After: \"" + token.get(AfterAnnotation.class) + "\"");
            }
        }
        System.out.println(rvFeatures.toString());
        System.out.println(tvFeatures.toString());
	}

}
