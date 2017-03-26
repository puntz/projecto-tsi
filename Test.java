package testes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.time.SUTime.Temporal;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class Test {

	public static void main(String[] args) {

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

		AnnotationPipeline aPipeline = new AnnotationPipeline();
		aPipeline.addAnnotator(new TimeAnnotator("sutime", props));

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// Create a new scanner to ready text from input
		Scanner scanner = new Scanner(System.in);

		System.out.println("Hey User, with who and when u wanna meeting?");

		// read some text in the text variable
		String text = scanner.nextLine();

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// gets today in format yyyy-MM-dd
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		document.set(CoreAnnotations.DocDateAnnotation.class, strDate);

		// run all Annotators on this text
		pipeline.annotate(document);
		aPipeline.annotate(document);

		// these are all the sentences in this document
		ArrayList<CoreMap> sentences = new ArrayList<CoreMap>(document.get(SentencesAnnotation.class));

		/* this list save the names of persons who will meet with the user */
		ArrayList<String> persons = new ArrayList<String>();
		/*
		 * this string save the names of the organization who will meet with the
		 * user
		 */
		String organization = null;
		/* this string save the misc of meeting */
		String misc = null;
		/*
		 * this string save the location of meeting. This value must be checked
		 */
		String location = new String("Undefined");
		/* this string save the date of meeting. */
		StringBuilder date = new StringBuilder();
		/* this string save the duration of meeting. */
		StringBuilder duration = new StringBuilder("Undefined");
		/* this string save the time of meeting. */
		StringBuilder hours = new StringBuilder();

		List<CoreMap> timexAnnsAll = document.get(TimeAnnotations.TimexAnnotations.class);
		
		// Variable to save the day of meeting if specified 
		String day = null;
		
		// Boolean to check if date has been added already
		Boolean added = false;
		
		if (timexAnnsAll != null) {
			for (CoreMap cm : timexAnnsAll) {

				Temporal tTemporal = cm.get(TimeExpression.Annotation.class).getTemporal();
				String temporal = tTemporal.toString();
				String[] strings = temporal.split("-");
				System.out.println(temporal);

				// this is the NER label of the token
				ArrayList<CoreLabel> tokens = (ArrayList<CoreLabel>) cm.get(TokensAnnotation.class);

				for (int i = 0; i < tokens.size(); i++) {

					// this is the text of the token
					String word = tokens.get(i).get(TextAnnotation.class);

					// this is the NER label of the token
					String ne = tokens.get(i).get(NamedEntityTagAnnotation.class);

					// set word to lower case
					String wordLowerCase = word.toLowerCase();

			
					if (ne.equals("DATE")) {

						day = ne;
						LocalDate lDate = new LocalDate(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]),
								Integer.parseInt(strings[2]));

						Period period = null;
						switch (wordLowerCase) {

						case "sunday":
							period = Period.fieldDifference(lDate, lDate.withDayOfWeek(DateTimeConstants.SUNDAY));
							break;

						case "monday":
							period = Period.fieldDifference(lDate, lDate.withDayOfWeek(DateTimeConstants.MONDAY));
							break;

						case "tuesday":
							period = Period.fieldDifference(lDate, lDate.withDayOfWeek(DateTimeConstants.TUESDAY));
							break;

						case "wednesday":
							period = Period.fieldDifference(lDate, lDate.withDayOfWeek(DateTimeConstants.WEDNESDAY));
							break;

						case "thursday":
							period = Period.fieldDifference(lDate, lDate.withDayOfWeek(DateTimeConstants.THURSDAY));
							break;

						case "friday":
							period = Period.fieldDifference(lDate, lDate.withDayOfWeek(DateTimeConstants.FRIDAY));
							break;

						case "saturday":
							period = Period.fieldDifference(lDate, lDate.withDayOfWeek(DateTimeConstants.SATURDAY));
							break;

						}

						int days = period.getDays();
						if (days < 1) {
							days = days + 7;
						}

						date.append(lDate.plusDays(days));
						
						String[] sTemporal = temporal.split("T");
						hours.append(sTemporal[1]);
						
						added = true;
						break;
					}
				}
				
				if(!added) {
					if (temporal.contains("T")) {
						String[] sTemporal = temporal.split("T");
						date.append(sTemporal[0]);
						hours.append(sTemporal[1]);
					}
					else {
						date.append(temporal);
					}
				}
				

				System.out.println(cm + " --> " + cm.get(TimeExpression.Annotation.class).getTemporal());

			}
		}
		
		System.out.println();

		// traversing the words in the current sentence
		for (CoreMap sentence : sentences) {

			ArrayList<CoreLabel> tokens = (ArrayList<CoreLabel>) sentence.get(TokensAnnotation.class);

			for (int i = 0; i < tokens.size(); i++) {

				// this is the text of the token
				String word = tokens.get(i).get(TextAnnotation.class);

				// this is the POS tag of the token
				String pos = tokens.get(i).get(PartOfSpeechAnnotation.class);

				String lemma = tokens.get(i).get(LemmaAnnotation.class);

				// this is the NER label of the token
				String ne = tokens.get(i).get(NamedEntityTagAnnotation.class);

				System.out.println("Word: " + word + " Pos: " + pos + " Ne: " + ne + " Lemma: " + lemma);

				 switch (pos) {
				
				 /**
				 * Cover date time and numbers that were unvalued.
				 */
				
//				 case "CD":
//				 if (tokens.get(i -
//				 1).get(NamedEntityTagAnnotation.class).equals("TIME")) {
//				
//				 Calendar cal = Calendar.getInstance();
//				 date.append(" " + word + " " + new
//				 SimpleDateFormat("MMM").format(cal.getTime()));
//				 }
//				
//				 break;
				 
				 }

				switch (ne) {

				/**
				 * Cover recognizes named (PERSON, LOCATION, ORGANIZATION, MISC)
				 */

				case "PERSON":
					persons.add(word);
					break;

				case "ORGANIZATION":
					organization = word;
					break;

				case "LOCATION":
					location = word;
					break;

				case "MISC":
					misc = word;
					break;

				/**
				 * Cover temporal (DATE, TIME, DURATION, SET) entities.
				 */

				case "DATE":
					if (date.length() == 0) {
						date.append(word);
					} else {
						date.append(" (" + word + ")");
					}
					break;

				case "DURATION":
					if (duration == null) {
						duration = new StringBuilder();
						duration.append(word);
					} else {
						duration.append(" " + word);
					}
					break;
				}

				// if (date == null) {
				// System.out.println("What time do you want to meet?");
				// scanner = new Scanner(System.in);
				// String in = scanner.nextLine();
				// document = new Annotation(in);
				// sentences = new
				// ArrayList<CoreMap>(document.get(SentencesAnnotation.class));
				//
				// for (CoreMap sentc : sentences) {
				// for (CoreLabel token : sentence.get(TokensAnnotation.class))
				// {
				// // this is the text of the token
				// String word = token.get(TextAnnotation.class);
				// // this is the POS tag of the token
				// String pos = token.get(PartOfSpeechAnnotation.class);
				//
				// // this is the NER label of the token
				// String ne = token.get(NamedEntityTagAnnotation.class);
				//
				// }
				// }
			}

			System.out.println("\n" + "Persons envolved in meeting: " + persons.toString() + "\n" + "Location: "
					+ location + "\n" + "Date: " + date + "\n" + "Duration: " + duration + "\n" + "Hours: " + hours);

			// close scanner
			scanner.close();

			/*
			 * Next we will extract the SemanitcGraph to examine the connection
			 * between the words in our evaluated sentence
			 */

			SemanticGraph dependencies = sentence.get(CollapsedDependenciesAnnotation.class);

//			System.out.println();
//			System.out.println("The first sentence basic dependencies are:");
//			SemanticGraph basicDependecies = sentence
//					.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//			for (TypedDependency dep : basicDependecies.typedDependencies()) {
//				System.out.println(dep.toString());
//			}
//
//			System.out.println();
//			System.out.println("The first sentence collapsed, CC-processed dependencies are:");
//			SemanticGraph processedDependencies = sentence
//					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//			for (TypedDependency dep : processedDependencies.typedDependencies()) {
//				// if(dep.reln().toString().equals("case"))
//				System.out.println(dep.toString());
//			}

		}
	}
}
