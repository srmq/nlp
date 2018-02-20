package br.ufpe.cin.srmqnlp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import br.cin.ufpe.nlp.util.ArrayUtil;
import br.cin.ufpe.nlp.util.Pair;
import edu.stanford.nlp.ling.CoreAnnotations.AfterAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.BeforeAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
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
	private TeXHyphenator teXHyphenator;
	private StanfordCoreNLP pipeline;
	
	public static class TextFeatures {
		private List<Pair<String[], double[]>> features;
		public TextFeatures(List<Pair<String[], double[]>> featureDescAndValues) {
			assert(featureDescAndValues != null);
			this.features = featureDescAndValues;
		}
		public List<Pair<String[], double[]>> getFeatures() {
			return features;
		}
	}
	
	/**
	 * 
	 * @param annotatedDocument has already be annotated by the pipeline
	 * @return
	 */
	public TextFeatures extractFeatures(Annotation annotatedDocument) {
        List<CoreMap> sentences = annotatedDocument.get(SentencesAnnotation.class);
        RelationalVisualFeatures rvFeatures = new RelationalVisualFeatures();
        TokenVisualFeatures tvFeatures = new TokenVisualFeatures();

        
        tvFeatures.numberOfSentences = sentences.size();
        tvFeatures.numberOfCharacters = annotatedDocument.get(TextAnnotation.class).length();
        Set<String> globalUniqueWords = new HashSet<String>();
        for(CoreMap sentence: sentences) {
            //SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);
            //IndexedWord root = dependencies.getFirstRoot();
            //System.out.printf("root(ROOT-0, %s-%d)%n", root.word(), root.index());
//            for (SemanticGraphEdge e : dependencies.edgeIterable()) {
//                System.out.printf ("%s(%s-%d, %s-%d)%n", e.getRelation().toString(), e.getGovernor().word(), e.getGovernor().index(), e.getDependent().word(), e.getDependent().index());
//            }

            tvFeatures._currentSentenceIndex = sentence.get(SentenceIndexAnnotation.class); 
        	this.addRelationalVisualFeatures(sentence, rvFeatures);
        	this.addTokenVisualFeatures(sentence, tvFeatures, globalUniqueWords);

//            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
//            	// this is the text of the token
//                String word = token.get(TextAnnotation.class);
//                int index = token.get(IndexAnnotation.class);
//                boolean [] breakPoints = featureExtractor.teXHyphenator.findBreakPoints(word.toLowerCase(Locale.ENGLISH).toCharArray());
//                
//                // this is the POS tag of the token
//                String pos = token.get(PartOfSpeechAnnotation.class);
//                // this is the NER label of the token
//                String ne = token.get(NamedEntityTagAnnotation.class);
//                System.out.print("[" + tvFeatures._currentSentenceIndex + ", " + index + "]:(" + word + ", " + pos + ", " + ne + ") ");
//                for (int i = 0; i < word.length(); i++) {
//                	if (breakPoints[i]) 
//                		System.out.print('-');
//                	System.out.print(word.charAt(i));
//                }
//                System.out.println(" Before: \"" + token.get(BeforeAnnotation.class) + "\" After: \"" + token.get(AfterAnnotation.class) + "\"");
//            }
        }
        ReadabilityFeatures readabFeatures = new ReadabilityFeatures(tvFeatures);
//        System.out.println(rvFeatures.toString());
//        System.out.println(tvFeatures.toString());
//        System.out.println(readabFeatures.toString());
       	TextFeatures txtFeatures = toTextFeatures(rvFeatures, tvFeatures, readabFeatures);
       	return txtFeatures;
		
	}
	
	public TextFeatures extractFeatures(String text) {
        Annotation document = pipelineAnnotate(text);
        return this.extractFeatures(document);
	}

	public Annotation pipelineAnnotate(String text) {
		Annotation document = new Annotation(text);
        this.pipeline.annotate(document);
		return document;
	}

	private TextFeatures toTextFeatures(RelationalVisualFeatures rvFeatures, TokenVisualFeatures tvFeatures,
			ReadabilityFeatures readabFeatures) {
		String[] relationalDescriptions = rvFeatures.getDescriptions();
		double[] relationalValues = rvFeatures.computeValues();
		String[] tokenVisualDescriptions = tvFeatures.getDescriptions();
		double[] tokenVisualValues = tvFeatures.computeValues();
		
		String[] visualPropertyDescriptions = ArrayUtil.concat(relationalDescriptions, tokenVisualDescriptions);
		double[] visualPropertyValues = ArrayUtil.concat(relationalValues, tokenVisualValues);
		
		List<Pair<String[], double[]>> features = new ArrayList<Pair<String[], double[]>>(2);
		features.add(new Pair<String[], double[]>(visualPropertyDescriptions, visualPropertyValues));
		features.add(new Pair<String[], double[]>(readabFeatures.getDescriptions(), readabFeatures.computeValues()));
		TextFeatures txtFeatures = new TextFeatures(features);
		return txtFeatures;
	}

	public FeatureExtractorTQ() {
		this.patternForURLs = TokenSequencePattern.compile(urlRegEx);
        teXHyphenator = new TeXHyphenator();
        teXHyphenator.loadDefault();
        
		Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, depparse");
        this.pipeline = new StanfordCoreNLP(props);
        
	}
	
	private static class RelationalVisualFeatures extends AbstractFeatures {
		private int auxVerbCount;
		private static final String[] mydescriptions = {"Count of auxiliary verbs"};
		public double[] computeValues() {
			return new double[]{auxVerbCount};
		}
		private RelationalVisualFeatures() {
			super(mydescriptions);
		}
	}
	
	private static class TokenVisualFeatures extends AbstractFeatures {
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
		
		private int _totalNumberOfSyllables;
		//complex words have 3 or more syllables
		private int _numOfComplexWords;
		
		private int _globalUniqueWords;
		private int _sentenceTotalUniqueWords;
		//more than 6 letters
		private int _numOfLongWords;
		private int _numOfShortSentences;
		private int _numOfLongSentences;
		
		private static final String[] mydescriptions = {"Count of pronouns", "Count of conjunctions",
				"Count of prepositions", "Count of occurences of the verb \"to be\"",
				"Count of punctuation marks", "Minimum length of quoted text",
				"Average length of quoted text", "Maximum length of quoted text",
				"Number of quotes", "Number of sentences", "Number of capitalized words",
				"Number of characters", "Number of whitespace violations",
				"Number of URLs", "Number of words", "Number of capitalization violations",
				"Number of question marks", "Number of punctuation violations",
				"Number of whitespaces", "Punctuation characters divided by all characters",
				"Whitespace characters divided by all characters",
				"Capital letters characters divided by all characters"};
		
		private TokenVisualFeatures() {
			super(mydescriptions);
		}
		
		public double[] computeValues() {
			double[] result = new double[descriptions.length];
			result[0] = pronounCount;
			result[1] = conjCount;
			result[2] = prepCount;
			result[3] = verbToBeCount;
		    result[4] = punctuationCount;
		    result[5] = minQuotedTextLength;
		    result[6] = ((numberQuotedTexts > 0) ? (_totalLengthQuotedText/(double)numberQuotedTexts) : 0);
		    result[7] = maxLengthQuotedText;
		    result[8] = numberQuotedTexts;
			result[9] = numberOfSentences;
			result[10] = capitalizedWords;
			result[11] = numberOfCharacters;
			result[12] = whitespaceViolations;
			result[13] = numberOfURLs;
		    result[14] = numberOfWords;
		    result[15] = capitalizationViolations;
		    result[16] = numberQuestionMarks;
		    result[17] = punctuationViolations;
		    result[18] = numberWhiteSpaces;
		    result[19] = ((numberOfCharacters > 0) ? (punctuationCount/(double)numberOfCharacters) : 0); 
		    result[20] = ((numberOfCharacters > 0) ? (numberWhiteSpaces/(double)numberOfCharacters) : 0);
		    result[21] = ((numberOfCharacters > 0) ? (_capitalLettersCharacters/(double)numberOfCharacters) : 0);
			assert(result.length == 22);
			return result;
		}
	}
	
	protected static abstract class AbstractFeatures {
		protected String[] descriptions;
		public abstract double[] computeValues();
		
		protected AbstractFeatures(String[] descriptions) {
			this.descriptions = descriptions;
		}
		
		public String toString() {
			double[] values = this.computeValues();
			StringBuffer stb = new StringBuffer();
			stb.append(descriptions[0]);
			stb.append(": ");
			stb.append(values[0]);
			for (int i = 1; i < descriptions.length; i++) {
				stb.append('\n');
				stb.append(descriptions[i]);
				stb.append(": ");
				stb.append(values[i]);
			}
			return stb.toString();
		}
		
		public String[] getDescriptions() {
			return descriptions;
		}
		
		
	}
	
	private static class ReadabilityFeatures extends AbstractFeatures {
		private static final String[] myDescriptions = {"Average words per sentence", 
				"Average word length in syllables",
				"Average word length in characters", 
				"Number of complex words divided by all words",
				"Number of unique words",
				"Average unique words per sentence",
				"Flesch-Kinkaid grade level",
				"Automated readability index",
				"Coleman-Liau index",
				"Flesch reading ease",
				"Gunning-Fog index",
				"LIX score",
				"SMOG grade",
				"Number of short sentences",
				"Number of long sentences"
				};
		private ReadabilityFeatures(TokenVisualFeatures tokenVisualFeatures) {
			super(myDescriptions);

			this.avgWordsPerSentence = tokenVisualFeatures.numberOfSentences != 0 ? ((double)tokenVisualFeatures.numberOfWords) / tokenVisualFeatures.numberOfSentences : 0;
			this.avgWordlengthInSyllables = tokenVisualFeatures.numberOfWords != 0 ? ((double)tokenVisualFeatures._totalNumberOfSyllables) / tokenVisualFeatures.numberOfWords : 0;
			this.avgWordlengthInCharacters = tokenVisualFeatures.numberOfWords != 0 ? ((double)tokenVisualFeatures.numberOfCharacters) / tokenVisualFeatures.numberOfWords : 0;
			this.complexWordsDivByAllWords = tokenVisualFeatures.numberOfWords != 0 ? ((double)tokenVisualFeatures._numOfComplexWords)/ tokenVisualFeatures.numberOfWords : 0;
			this.numberOfUniqueWords = tokenVisualFeatures._globalUniqueWords;
			this.avgUniqueWordsPerSentence = tokenVisualFeatures.numberOfSentences != 0 ? ((double)tokenVisualFeatures._sentenceTotalUniqueWords) / tokenVisualFeatures.numberOfSentences : 0;
			this.fleschKinkaidGradeLevel = (tokenVisualFeatures.numberOfWords != 0 && tokenVisualFeatures.numberOfSentences != 0) ? 0.39*(((double)tokenVisualFeatures.numberOfWords) / tokenVisualFeatures.numberOfSentences) + 11.8*(((double)tokenVisualFeatures._totalNumberOfSyllables)/tokenVisualFeatures.numberOfWords) - 15.59 : 0;		
			this.automatedReadabilityIndex = (tokenVisualFeatures.numberOfWords != 0 && tokenVisualFeatures.numberOfSentences != 0) ? 4.71*(((double)tokenVisualFeatures.numberOfCharacters)/tokenVisualFeatures.numberOfWords) + 0.5*(((double)tokenVisualFeatures.numberOfWords)/tokenVisualFeatures.numberOfSentences) - 21.43 : 0;
			this.colemanLiauIndex = (tokenVisualFeatures.numberOfWords != 0) ? 0.0588*(((double)tokenVisualFeatures.numberOfCharacters)/tokenVisualFeatures.numberOfWords*100.0) - 0.296*(((double)tokenVisualFeatures.numberOfSentences)/tokenVisualFeatures.numberOfWords*100.0) - 15.8 : 0;
			this.fleschReadingEase = (tokenVisualFeatures.numberOfWords != 0 && tokenVisualFeatures.numberOfSentences != 0) ? 206.835 - 1.015*(((double)tokenVisualFeatures.numberOfWords)/tokenVisualFeatures.numberOfSentences) - 84.6*(((double)tokenVisualFeatures._totalNumberOfSyllables)/tokenVisualFeatures.numberOfWords) : 0;
			this.gunningFogIndex = (tokenVisualFeatures.numberOfWords != 0 && tokenVisualFeatures.numberOfSentences != 0) ? 0.4*( (((double)tokenVisualFeatures.numberOfWords) / tokenVisualFeatures.numberOfSentences) + 100.0*(((double)tokenVisualFeatures._numOfComplexWords)/ tokenVisualFeatures.numberOfWords) ) : 0;
			this.lIXScore = (tokenVisualFeatures.numberOfWords != 0 && tokenVisualFeatures.numberOfSentences != 0) ? ((double)tokenVisualFeatures.numberOfWords)/tokenVisualFeatures.numberOfSentences + (tokenVisualFeatures._numOfLongWords*100.0)/tokenVisualFeatures.numberOfWords : 0;
			this.sMOGGrade = 1.0430*Math.sqrt(tokenVisualFeatures._numOfComplexWords*(30.0/tokenVisualFeatures.numberOfSentences)) + 3.1291;
			this.numberOfShortSentences = tokenVisualFeatures._numOfShortSentences;
			this.numberOfLongSentences = tokenVisualFeatures._numOfLongSentences;
		}

		private double avgWordsPerSentence;
		private double avgWordlengthInSyllables;
		private double avgWordlengthInCharacters;
		private double complexWordsDivByAllWords;
		private int numberOfUniqueWords;
		private double avgUniqueWordsPerSentence;

		private double fleschKinkaidGradeLevel;
		private double automatedReadabilityIndex;
		private double colemanLiauIndex;
		private double fleschReadingEase;
		private double gunningFogIndex;
		private double lIXScore;
		private double sMOGGrade;
		//less than 12 words
		private double numberOfShortSentences;
		//more than 20 words
		private double numberOfLongSentences;
		
		
		@Override
		public double[] computeValues() {
			double[] result = new double[descriptions.length];
			result[0] = this.avgWordsPerSentence;
			result[1] = this.avgWordlengthInSyllables;
			result[2] = this.avgWordlengthInCharacters;
			result[3] = this.complexWordsDivByAllWords;
			result[4] = this.numberOfUniqueWords; 
			result[5] = this.avgUniqueWordsPerSentence;
			result[6] = this.fleschKinkaidGradeLevel;
			result[7] = this.automatedReadabilityIndex;
			result[8] = this.colemanLiauIndex;
			result[9] = this.fleschReadingEase;
			result[10] = this.gunningFogIndex;
			result[11] = this.lIXScore;
			result[12] = this.sMOGGrade;
			result[13] = this.numberOfShortSentences;
			result[14] = this.numberOfLongSentences;
			assert (result.length == 15);
			return result;
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
	
	private void addTokenVisualFeatures(CoreMap sentence, TokenVisualFeatures features, Set<String> globalUniqueWords) {
		List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
		int numTokens = tokens.size();
		Set<String> urlS = computeNumberOfURLs(features, tokens);
		Set<String> sentenceUniqueWords = new HashSet<String>(numTokens);
		int numWordsThisSentence = 0;
        for (CoreLabel token: tokens) {
        	//token indices are 1-based
        	final int tokenIndex = token.get(IndexAnnotation.class);
            // this is the POS tag of the token
            String pos = token.get(PartOfSpeechAnnotation.class);
            
        	// this is the text of the token
            String word = token.get(TextAnnotation.class);
            
            
            if (posCountsAsWord(pos) && !urlS.contains(word)) {
            	final String lCaseWord = word.toLowerCase(Locale.ENGLISH);
            	features.numberOfWords++;
                boolean [] breakPoints = teXHyphenator.findBreakPoints(lCaseWord.toCharArray());
                final int numSyllables = numberOfSyllables(breakPoints);
                features._totalNumberOfSyllables += numSyllables;
                if (numSyllables >= 3) {
                	features._numOfComplexWords++;
                }
                globalUniqueWords.add(lCaseWord);
                sentenceUniqueWords.add(lCaseWord);
                if (lCaseWord.length() > 6) features._numOfLongWords++;
                numWordsThisSentence++;
            }
            
            
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
        features._globalUniqueWords = globalUniqueWords.size();
        features._sentenceTotalUniqueWords += sentenceUniqueWords.size();
        if (numWordsThisSentence < 12)
        	features._numOfShortSentences++;
        else if (numWordsThisSentence > 20)
        	features._numOfLongSentences++;
	}

	private int numberOfSyllables(boolean[] breakPoints) {
		int numSyllables = 0;
		for (int i = 0; i < breakPoints.length; i++) {
			if (breakPoints[i]) 
				numSyllables++;
		}
		return numSyllables;
	}

	private Set<String> computeNumberOfURLs(TokenVisualFeatures features, List<CoreLabel> tokens) {
		TokenSequenceMatcher matcher = patternForURLs.getMatcher(tokens);
		Set<String> urlsMatched = new HashSet<String>();
		while (matcher.find()) {
			// System.out.println(">>>MATCHED: " + matcher.group());
			urlsMatched.add(matcher.group());
			features.numberOfURLs++;
		}
		return urlsMatched;
	}
	
	public static void main(String[] args) {
		FeatureExtractorTQ featureExtractor = new FeatureExtractorTQ();

        String text = "i was  dancing the polka ,do you dance the polka ? I [do] think so. please visit https://www.facebook.com/sergio.queiroz and http://stackoverflow.com/questions/163360/regular-expression-to-match-urls-in-java. Hello? Are you there?"; // Add your text here!
        TextFeatures txtFeatures = featureExtractor.extractFeatures(text);
        List<Pair<String[], double[]>> featList = txtFeatures.getFeatures();
        for (Pair<String[], double[]> pair : featList) {
			System.out.println("------------------");
			assert(pair.getFirst().length == pair.getSecond().length);
			for (int i = 0; i < pair.getFirst().length; i++) {
				System.out.println(pair.getFirst()[i] + ": " + pair.getSecond()[i]);
			}
		}
	}

}
