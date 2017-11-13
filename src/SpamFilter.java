/* Names: Christopher Fitzgerald, Jacob Huiet, Mate Virag
 * Programming Assignment #4
 * Professor Fox
 * Class: CSC 425
 * Date: 4/28/2017
 * Description: This program identifies whether an email is spam or not by using a naive Bayesian classifier. The system is trained on 350
 * spam and 350 non-spam emails, and then is used to determine whether the test emails are spam or non-spam.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SpamFilter {
	
	public static void main(String args[]) {
		HashMap<String, Integer> spamWords = new HashMap<String, Integer>();	//hashmap to keep track of the words and their occurrences in the spam training set
																				//the key is the word and the value is the occurrence
		HashMap<String, Integer> hamWords = new HashMap<String, Integer>();		//hashmap to keep track of the words and their occurrences in the non-spam training set
		int spamEmailCounter = populateHashMap("ex6DataEmails/spam-train", spamWords);	//populates the spam hashset with the training data
		int hamEmailCounter = populateHashMap("ex6DataEmails/nonspam-train", hamWords);	//populates the non-spam hashset with the training data
		System.out.println("Spam emails:\n");
		getTestEmails("ex6DataEmails/spam-test", spamWords, hamWords, spamEmailCounter);	//tests the spam test set
		System.out.println("\nHam emails:\n");
		getTestEmails("ex6DataEmails/nonspam-test", spamWords, hamWords, hamEmailCounter);	//tests the non-spam test set
	}
	
	/*
	 * Populates the hash maps with data from the training sets. Returns the number of emails in each training set that will be used when
	 * calculating the probabilities at the end.
	 */
	public static int populateHashMap(String location, HashMap<String, Integer> hmap) {
		// Uses the folder location that is passed in to the function to locate the training data
		File folder = new File(location);
		File[] listOfFiles = folder.listFiles();	// Lists all the files in the given folder
		
		int counter = 0;	// Keeps track of the number of emails in the given set
		String fileName = "";	//Stores the file name and location
		String wholeText = "";	//Stores the given email as a whole
		BufferedReader br;
		StringBuilder sb;
		
		//Iterates through all the files in the folder
		for (File file : listOfFiles) {
			if (file.isFile()) {
				fileName = location + "/" + file.getName();	//Gets the exact location of each file
				try {
					br = new BufferedReader(new FileReader(fileName));
					sb = new StringBuilder();
					String line = br.readLine();	//Reads from the file line by line
					
					while (line != null) {
						sb.append(line);	//Reads from the file line by line until the end of file
						line = br.readLine();	//and appends it to the string builder
					}
					wholeText = sb.toString();	//makes the input text a string
					br.close();
					
					String[] splitText = wholeText.split(" "); //splits the text up by spaces and puts it in an array
					ArrayList<String> usedWords = new ArrayList<String>();	//keeps track of the words that we have already seen in the given email
																			//so we don't increment its occurrence twice from the same email 
					
					for (String ss : splitText) {	//goes through each word in the email and if it's at least 1 character long
						if (ss.length() > 0) {		//and if it hasn't been seen in the email, it adds it to the hash map and then to the used words arraylist
							if(usedWords.contains(ss) == false) {
								usedWords.add(ss);
								insertIntoHashMap(ss, hmap);
							}
						}
					}
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				counter++;	//increments as each training email is read in
			}
		}
		return counter;	//returns the number of emails in the set
	}
	
	//Takes a word and inserts it into one of the given training hashmaps
	public static void insertIntoHashMap(String word, HashMap<String, Integer> hmap) {
		int count = 0;	//counts how many times the word has shown up in the training set
		
		if (hmap.get(word) != null) { //checks if the word has appeared before
			count = hmap.get(word); //get the number of times the word has shown up earlier if it has already shown up
			count++;	//increment by one
			hmap.put(word, count); //put back the word with the new value into the hashmap
		}
		else
			hmap.put(word,1); //if new word, put into hashmap with a value of 1
	}
	
	/*
	 * Gets the test emails and passes them to another function that determines whether the email is spam or ham
	 */
	public static void getTestEmails(String location, HashMap<String, Integer> spamHmap, HashMap<String, Integer> hamHmap, int emailCounter) {
		//Uses the folder location that is passed in to the function to locate the training data
		File folder = new File(location);
		File[] listOfFiles = folder.listFiles();	// Lists all the files in the given folder
		
		String fileName = "";	
		String wholeText = "";	//Stores the given email as a whole
		BufferedReader br;
		StringBuilder sb;
		boolean isSpam;
		int spamCounter = 0;
		int emailCount = 0;
		
		//Uses the same method as in the function above to get the test emails
		for (File file : listOfFiles) {
			if (file.isFile()) {
				fileName = location + "/" + file.getName();
				try {
					br = new BufferedReader(new FileReader(fileName));
					sb = new StringBuilder();
					String line = br.readLine();
					
					while (line != null) {
						sb.append(line);
						line = br.readLine();
					}
					
					wholeText = sb.toString();
					br.close();
					
					String[] splitText = wholeText.split(" ");
					isSpam = spamOrHam(splitText, spamHmap, hamHmap, emailCounter);	//Calls the spamOrHam function to determine whether the given test email is spam
					if (isSpam)
						spamCounter++;	//Keeps track of the number of spam emails in the test set
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				emailCount++;
			}
		}
		//System.out.println("Number of spams in the set: " + spamCounter); //outputs the number of spams in the test set
		//outputs the percentage of spams in the test set
		//System.out.println("Percentage of spams in the set: " + (((double)spamCounter / emailCount) * 100) + "%");
	}
	
	//takes the email in the form of a array of strings
	//the hashmap that we trained for spam and ham
	//and the total number of training e mails
	//Calculates spam or ham and returns true if the email is spam and false if it isn't
	public static boolean spamOrHam(String[] mail, HashMap<String, Integer> spamHmap, HashMap<String, Integer> hamHmap, int count){
		double probOfSpam = 0.5; //this is p(h), ours is defaulted to .5 because we had the same number of spam and ham training e mail
		double probOfHam = 0.5;	//if you didn't have an even set it would be the number of spam or ham / by the total number of e mails
		double spamCount, hamCount; //Each of these counts the number of times each word appeared in the ham or the spam set
		String word;
		
		for (int i = 0; i < mail.length; i++) { //For each word in the email
			
			word = mail[i]; //gets the word we will be working with from array
			
			if (spamHmap.get(word) != null && hamHmap.get(word) != null) { //checks to see if word is in both hashmaps, if not, it discards the word because
																			//that would make the probabilities inaccurate
				spamCount = spamHmap.get(word); //gets the number of spam e mails the word showed up in
				double spamCal = spamCount / count; //gets the p(w|e)for the word
				probOfSpam = probOfSpam * spamCal; //this is the running calculation for p(h|e)
			
				hamCount = hamHmap.get(word); //same as above but for ham
				double hamCal = hamCount / count;
				probOfHam = probOfHam * hamCal;
				if (probOfHam < Math.pow(10, -200) || probOfSpam < Math.pow(10, -200)) { //checks if either probability falls below a certain value and
					probOfSpam *= 1000000;					//multiplies both probabilities so we never end up with such small values that they become 0
					probOfHam *= 1000000;
				}
			}
		}
		
		//checks which probability is higher and then determines whether the email is spam or not
		if (probOfSpam > probOfHam) {
			System.out.println("This is a spam email");
			return true;
		}
		else {
			System.out.println("This is a non-spam email");
			return false;
		}
	}
}