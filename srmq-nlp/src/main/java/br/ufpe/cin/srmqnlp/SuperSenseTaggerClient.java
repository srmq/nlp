package br.ufpe.cin.srmqnlp;

import java.util.List;

import org.springframework.web.client.RestTemplate;

import br.cin.ufpe.nlp.util.AnnotatedDocument;
import br.cin.ufpe.nlp.util.AnnotatedSentence;
import br.cin.ufpe.nlp.util.AnnotatedToken;
import br.cin.ufpe.nlp.util.TokenAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class SuperSenseTaggerClient {

	public static void main(String[] args) {
		FeatureExtractorTQ featureExtractor = new FeatureExtractorTQ();
        String text = "i was  dancing the polka ,do you dance the polka ? I [do] think so. please visit https://www.facebook.com/sergio.queiroz and http://stackoverflow.com/questions/163360/regular-expression-to-match-urls-in-java. Hello? Are you there?"; // Add your text here!
        Annotation annotation = featureExtractor.pipelineAnnotate(text);
		AnnotatedDocument doc = new AnnotatedDocument();
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
			AnnotatedSentence annotSentence = new AnnotatedSentence();
			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
	        for (CoreLabel token: tokens) {
	        	// this is the text of the token
	            String word = token.get(TextAnnotation.class);
	        	AnnotatedToken annotToken = new AnnotatedToken(word);
	            String pos = token.get(PartOfSpeechAnnotation.class);
	            annotToken.addAnnotation(TokenAnnotation.POSTAG, pos);
	            annotSentence.addToken(annotToken);
	        }
	        doc.addSentence(annotSentence);
		}
		RestTemplate restTemplate = new RestTemplate();
		AnnotatedDocument result = restTemplate.postForObject("http://localhost:8081/supersenses/", doc, AnnotatedDocument.class);
		System.out.println(result);
	}
}
