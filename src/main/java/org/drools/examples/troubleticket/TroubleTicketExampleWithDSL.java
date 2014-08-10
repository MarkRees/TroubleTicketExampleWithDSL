package org.drools.examples.troubleticket;

/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.compiler.DroolsParserException;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
//import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.conf.SingleValueKnowledgeBuilderOption;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.logger.KnowledgeRuntimeLogger;
import org.kie.internal.logger.KnowledgeRuntimeLoggerFactory;
import org.kie.api.io.ResourceType;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.api.runtime.rule.FactHandle;

public class TroubleTicketExampleWithDSL {

	//This is good stuff:
	//http://members.inode.at/w.laun/drools/KnowledgeBaseSetupHowTo.html

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		//this is the old way, isn't it?
		final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

		// final KieBuilder kbuilder = new KieBuilder(); //KnowledgeBuilderFactory.newKnowledgeBuilder();

		//find out where we are
		System.out.println("Working Directory = " + System.getProperty("user.dir"));

		//compile the rules        

		kbuilder.add( ResourceFactory.newClassPathResource( "rules/ticketing.dsl",
				TroubleTicketExampleWithDSL.class ),ResourceType.DSL );
		kbuilder.add( ResourceFactory.newClassPathResource("rules/TroubleTicketWithDSL.dslr",
		        TroubleTicketExampleWithDSL.class), ResourceType.DSLR );

//		kbuilder.add( ResourceFactory.newClassPathResource("rules/rule3.dslr",
//				TroubleTicketExampleWithDSL.class), ResourceType.DSLR);

	
		System.err.println(kbuilder.toString());

		// Check for errors
		System.err.println(kbuilder.hasErrors());
		if( kbuilder.hasErrors() ){
			for( KnowledgeBuilderError err: kbuilder.getErrors() ){
				System.err.println( err.toString() );
			}
			throw new IllegalStateException( "DRL errors" );
		}

		//        File tmpDir = new File( "/tmp/drools-build" );
		//        SingleValueKnowledgeBuilderOption ddOption = DumpDirOption.get( tmpDir );
		//        KnowledgeBuilderConfiguration kbConfig =
		//            KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
		//        kbConfig.setOption( ddOption );
		//        KnowledgeBuilder kbuilder2 = KnowledgeBuilderFactory.newKnowledgeBuilder( kbConfig );


		//Create the Knowledge Base (Where rules are loaded)
		final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

		//Add the compiled Knowledge Packages (Rules)
		kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

		//Create a place to put the facts
		final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

		KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "trouble_ticket");

		final Customer a = new Customer( "A",
				"Drools",
				"Gold" );
		final Customer b = new Customer( "B",
				"Drools",
				"Platinum" );
		final Customer c = new Customer( "C",
				"Drools",
				"Silver" );
		final Customer d = new Customer( "D",
				"Drools",
				"Silver" );

		final Ticket t1 = new Ticket( a );
		final Ticket t2 = new Ticket( b );
		final Ticket t3 = new Ticket( c );
		final Ticket t4 = new Ticket( d );
		t4.setStatus("New");

		System.err.println(d.toString());
		System.err.println(t4.toString());


		ksession.insert( a );
		ksession.insert( b );
		ksession.insert( c );
		ksession.insert( d );

		ksession.insert( t1 );
		ksession.insert( t2 );
		final FactHandle ft3 = ksession.insert( t3 );
		ksession.insert( t4 );

		ksession.fireAllRules();

		t3.setStatus( "Done" );

		ksession.update( ft3, t3 );

		//        try {
		//            System.err.println( "[[ Sleeping 5 seconds ]]" );
		//            Thread.sleep( 1000 );
		//        } catch ( final InterruptedException e ) {
		//            e.printStackTrace();
		//        }

		System.err.println( "[[ awake ]]" );

		ksession.fireAllRules();

		ksession.dispose();

		logger.close();

//		myParser("rule3.dslr", "ticketing.dsl");
		myParser("TroubleTicketWithDSL.dslr", "ticketing.dsl");
		
		
	}

	// This function creates a .dlr from a .dsl and .dslr

	private static void myParser(String dslr, String dsl)  {

		DrlParser parser = new DrlParser();
		String result = null;
		String mydsl2 = null;
		String mydslr2 = null;

		{

			// Read the DSLR
			try {
				//System.out.println("-----------------rule3.dslr");
				byte[] x = Files.readAllBytes(Paths.get("src/main/resources/rules/" + dslr));
				mydslr2 = new String ( x);
				//System.out.println(mydslr2);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			//Read the DSL
			try {
				//System.out.println("-----------------ticketing.dsl");
				byte[] y = Files.readAllBytes(Paths.get("src/main/resources/rules/" + dsl));
				mydsl2 = new String ( y);
				//System.out.println(mydsl2);
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			//Expand it
			try {
				result = parser.getExpandedDRL(  mydslr2, new StringReader(mydsl2));
				//System.out.println("-----------------RESULT");
				//System.out.println(result);
				

				File file= new File("src/main/resources/rules/rule3.drl.txt");
				BufferedWriter output = new BufferedWriter(new FileWriter(file));
				output.write("//Automatically Generated by myParser() " + new Date().toString() +"\n\n"+result);
				output.close();
				
//				byte[] x = result.getBytes();
//				Files.write(Paths.get("src/main/resources/rules/rule3.drl.txt"), x,  	OpenOption)
			} catch (DroolsParserException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}


