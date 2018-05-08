package ddejonge.bandana.exampleAgents;


import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import ddejonge.bandana.anac.ANACNegotiator;
import ddejonge.bandana.dbraneTactics.DBraneTactics;
import ddejonge.bandana.negoProtocol.*;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.orders.Order;
import ddejonge.bandana.anac.ANACNegotiator;
import ddejonge.bandana.dbraneTactics.DBraneTactics;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyNegoClient;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.orders.MTOOrder;
import es.csic.iiia.fabregues.dip.orders.Order;
import es.csic.iiia.fabregues.dip.orders.SUPMTOOrder;

public class MyBot extends ANACNegotiator {


    /**
     * Main method to start the agent.
     * <p>
     * This player can be started with the following arguments:
     * -name  	[the name of your agent]
     * -log		[the path to the folder where you want the log files to be stored]
     * -fy 		[the year after which your agent will propose a draw]
     * -gamePort  [the port of the game server]
     * -negoPort  [the port of the negotiation server]
     * <p>
     * e.g. java -jar MyBot.jar -name alice -log C:\\documents\log -fy 1920 -gamePort 16713 -negoPort 16714
     * <p>
     * All of these arguments are optional.
     * <p>
     * Note however that during the competition the values of these arguments will be chosen by the organizers
     * of the competition, so you can only control them during the development of your negotiator.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {

        MyBot myPlayer = new MyBot(args);
        myPlayer.run();

    }

    private Vector<Power> m_coallition = new Vector<Power>();
    boolean m_isFirstTurn=true;
    DBraneTactics dBraneTactics;
    private int m_coalitioNum;

    public void addToCoallition(Power power)
    {
        boolean isAllyFounded = false;
        for(Power ally:m_coallition)
        {
            if(ally == power)
            {
                isAllyFounded = true;
            }
        }
        if(!isAllyFounded)
        {
            this.getLogger().log("ally not found " +power, true);
            m_coallition.add(power);
        }
        this.getLogger().log("ally found " +power, true);

    }


    //Constructor

    /**
     * You must implement a Constructor with exactly this signature.
     * The body of the Constructor must start with the line <code>super(args)</code>
     * but below that line you can put whatever you like.
     *
     * @param args
     */
    public MyBot(String[] args) throws IOException {
        super(args);
        dBraneTactics = this.getTacticalModule();
        boolean gotCoallitionNum = false;
       for(int i = 0; i < args.length; ++i)
       {
            if (args[i].equals("-CoalitionNum") && args.length > i + 1) {
                m_coalitioNum = Integer.parseInt(args[i+1]);
                gotCoallitionNum = true;
            }
       }
       if(!gotCoallitionNum)
       {
           throw new IOException();
       }
    }
    /**
     * This method is automatically called at the start of the game, after the 'game' field is set.
     * <p>
     * It is called when the first NOW message is received from the game server.
     * The NOW message contains the current phase and the positions of all the units.
     * <p>
     * You are allowed, but not required, to implement this method
     */
    @Override
    public void start() {

        //You can use the logger to write stuff to the log file.
        //The location of the log file can be set through the command line option -log.
        // it is not necessary to call getLogger().enable() because this is already automatically done by the ANACNegotiator class.

        boolean printToConsole = true; //if set to true the text will be written to file, as well as printed to the standard output stream. If set to false it will only be written to file.
        this.getLogger().logln("game is starting6676!", printToConsole);

    }


    @Override
    public void negotiate(long negotiationDeadline) {

        this.getLogger().logln("starting negotiate ", true);
        boolean startOfThisNegotiation = true;
        //This loop repeats 2 steps. The first step is to handle any incoming messages,
        // while the second step tries to find deals to propose to the other negotiators.
        while (System.currentTimeMillis() < negotiationDeadline + 800) {


            //STEP 1: Handle incoming messages.


            //See if we have received any message from any of the other negotiators.
            // e.g. a new proposal or an acceptance of a proposal made earlier.
            while (hasMessage()) {
                //Warning: you may want to add some extra code to break out of this loop,
                // just in case the other agents send so many proposals that your agent can't get
                // the chance to make any proposals itself.

                //if yes, remove it from the message queue.
                Message receivedMessage = removeMessageFromQueue();
                this.getLogger().logln("got meesage " + receivedMessage.getContent(), true);
                if (receivedMessage.getPerformative().equals(DiplomacyNegoClient.ACCEPT)) {
                    DiplomacyProposal acceptedProposal = (DiplomacyProposal)receivedMessage.getContent();
                    this.getLogger().logln("MyBot.negotiate() Received acceptance from " + receivedMessage.getSender() + ": " + acceptedProposal, true);

                    // Here we can handle any incoming acceptances.
                    // This random negotiator doesn't do anything with such messages however.

                    // Note: if a certain proposal has been accepted by all players it is still not considered
                    // officially binding until the protocol manager has sent a CONFIRM message.

                    // Note: if all agents involved in a proposal have accepted the proposal, then you will not receive an ACCEPT
                    // message from the last agent that accepted it. Instead, you will directly receive a CONFIRM message from the
                    // Protocol Manager.

                } else if (receivedMessage.getPerformative().equals(DiplomacyNegoClient.PROPOSE)) {

                    DiplomacyProposal receivedProposal = (DiplomacyProposal) receivedMessage.getContent();

                    BasicDeal deal = (BasicDeal) receivedProposal.getProposedDeal();

                    boolean outDated = false;

                    for (DMZ dmz : deal.getDemilitarizedZones()) {

                        // Sometimes we may receive messages too late, so we check if the proposal does not
                        // refer to some round of the game that has already passed.
                        if (isHistory(dmz.getPhase(), dmz.getYear())) {
                            outDated = true;
                            break;
                        }

                        //TODO: decide whether this DMZ is acceptable or not (in combination with the rest of the proposed deal).
						/*
						List<Power> powers = dmz.getPowers();
						List<Province> provinces = dmz.getProvinces();
						*/

                    }

                    //If the deal is not outdated, then check that it is consistent with the deals we are already committed to.
                    String consistencyReport = null;
                    if (!outDated) {

                        List<BasicDeal> commitments = new ArrayList<BasicDeal>();
                        commitments.addAll(this.getConfirmedDeals());
                        commitments.add(deal);
                        consistencyReport = Utilities.testConsistency(game, commitments);


                    }

                    if (!outDated && consistencyReport == null) {


                        this.acceptProposal(receivedProposal.getId());
                    }


                } else if (receivedMessage.getPerformative().equals(DiplomacyNegoClient.CONFIRM)) {
                    DiplomacyProposal confirmedProposal = (DiplomacyProposal) receivedMessage.getContent();
                    // The protocol manager confirms that a certain proposal has been accepted by all players involved in it.
                    // From now on we consider the deal as a binding agreement.
                    this.getLogger().logln("MyBot.negotiate() Received confirmed from " + receivedMessage.getSender() +
                            ": " + confirmedProposal, true);

                    // This is to make sure this happens only in the first time.
                    if (m_isFirstTurn) {
                        List<String> participitants = confirmedProposal.getParticipants();
                        for (String powerName : participitants) {
                            addToCoallition(this.game.getPower(powerName));
                        }
                    }


                } else if (receivedMessage.getPerformative().equals(DiplomacyNegoClient.REJECT)) {

                    DiplomacyProposal rejectedProposal = (DiplomacyProposal) receivedMessage.getContent();

                    // Some player has rejected a certain proposal.
                    // This example agent doesn't do anything with such messages however.

                    //If a player first accepts a proposal and then rejects the same proposal the reject message cancels
                    // his earlier accept proposal.
                    // However, this is not true if the reject message is sent after the Notary has already sent a confirm
                    // message for that proposal. Once a proposal is confirmed it cannot be undone anymore.
                } else {

                    //We have received any other kind of message.


                }

            }

            if(startOfThisNegotiation)
            {
                //STEP 2:  offer propisitions.
                List<BasicDeal> dealsToOffer = getDealsToOffer();
                for (BasicDeal deal : dealsToOffer) {
                    this.proposeDeal(deal);
                }
                List<BasicDeal> supportToOffer = getSupportRequest();
                for (BasicDeal deal : supportToOffer) {
                    this.proposeDeal(deal);
                }
            }
            startOfThisNegotiation = false;



            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }

        }
        m_isFirstTurn = false;


        //whenever you like, you can also propose a draw to all other surviving players:
        //this.proposeDraw();
    }

    // This function returns all the coallition members.
    private ArrayList<Power> getAlliveAllies() {
        ArrayList<Power> alliveAllies = new ArrayList<Power>();
        for (Power ally : m_coallition) {
            if (this.getNegotiatingPowers().contains(ally) && !ally.equals(me)) {
                alliveAllies.add(ally);
            }
        }
        return alliveAllies;
    }

    private ArrayList<BasicDeal> getDealsToOffer() {
        ArrayList<BasicDeal> dealsToOffer = new ArrayList<BasicDeal>();
        List<Power> powersToConisderAllies = getAlliveAllies();
        if(m_isFirstTurn)
        {
            for(Power power: this.game.getPowers())
            {
                if(power ==me)
                {
                    continue;
                }
                //This agent only generates deals for the current year and phase.
                // However, you can pick any year and phase here, as long as they do not lie in the past.
                // (actually, you can also propose deals for rounds in the past, but it doesn't make any sense
                //  since you obviously cannot obey such deals).
                ArrayList<DMZ> demilitarizedZones = new ArrayList<>();
                ArrayList<Power> oneAllyVector = new ArrayList<>();
                oneAllyVector.add(power);
                demilitarizedZones.add(new DMZ(game.getYear(), game.getPhase(), oneAllyVector,
                        me.getOwnedSCs()));
                ArrayList<Power> meAsVector = new ArrayList<>();
                meAsVector.add(me);
                demilitarizedZones.add(new DMZ(game.getYear(), game.getPhase(), meAsVector,
                        power.getOwnedSCs()));
                List<OrderCommitment> randomOrderCommitments = new ArrayList<>();
                BasicDeal deal = new BasicDeal(randomOrderCommitments, demilitarizedZones);
                dealsToOffer.add(deal);
            }
        }
        else
        {
            ArrayList<Power> alliveAllies = getAlliveAllies();
            // make offers for all the coallition members to not attack one of the coallition.
            for (int alliveAllyIndex = 0; alliveAllyIndex < alliveAllies.size(); alliveAllyIndex++) {
                //1. Create a list of allive alies powers
                ArrayList<Power> relevant_powers = new ArrayList<>();
                //1a. add myself to the list
                relevant_powers.add(me);
                Vector<Province> allProvinces = this.game.getProvinces();
                for (int i = 0; i < alliveAllies.size(); i++) {
                    // We want to make the offer to all the other coalition except the one it is relevant to.
                    if (i == alliveAllyIndex) {
                        continue;
                    }
                    relevant_powers.add(alliveAllies.get(i));
                }
                ArrayList<DMZ> demilitarizedZones = new ArrayList<DMZ>();
                demilitarizedZones.add(new DMZ(game.getYear(), game.getPhase(), relevant_powers,
                        alliveAllies.get(alliveAllyIndex).getOwnedSCs()));
                List<OrderCommitment> randomOrderCommitments = new ArrayList<>();
                BasicDeal deal = new BasicDeal(randomOrderCommitments, demilitarizedZones);
                dealsToOffer.add(deal);
            }

        }
        return dealsToOffer;
    }

    private ArrayList<BasicDeal> getSupportRequest()
    {
        ArrayList<BasicDeal> dealsToOffer = new ArrayList<BasicDeal>();
        ArrayList<Power> alliveAllies = getAlliveAllies();

        List<OrderCommitment> orderCommitments = new ArrayList<>();
        for(Region myRegion: this.me.getControlledRegions())
        {
            ArrayList<Region> adjacentSCEnemy = getAdjacentSCEnemy(myRegion);

            //this.getLogger().logln("ennemy of : " + myRegion.toString() + " is : " + adjacentSCEnemy.toString(), true);

            for(Region scEnemy : adjacentSCEnemy){
                for(Region adjacentOfEnemy : scEnemy.getAdjacentRegions()){
                    for (Power ally: alliveAllies){
                        if (ally.isControlling(adjacentOfEnemy)){
                            OrderCommitment order = makeSupportOrder(myRegion,scEnemy,ally,adjacentOfEnemy);

                            /*this.getLogger().logln("order1: " + "me : " + me.toString() +
                                    " location1 : " + myRegion.toString() +
                                    " dest1 : " + scEnemy.toString(), true);
                            this.getLogger().logln("order2: " + "ally : " + ally.toString() +
                                    " location2 : " + adjacentOfEnemy.toString() , true);*/

                            orderCommitments.add(order);
                            break;

                        }
                    }
                }
            }
            ArrayList<DMZ> demilitarizedZones = new ArrayList<DMZ>();
            if (orderCommitments.size() != 0){
                BasicDeal deal = new BasicDeal(orderCommitments, demilitarizedZones);
                dealsToOffer.add(deal);
            }

        }
        return dealsToOffer;
    }
    private ArrayList<Region> getAdjacentSCEnemy(Region region){
        ArrayList<Power> alliveAllies = getAlliveAllies();
        ArrayList<Region> adjSCEnemy = new ArrayList<>();
        alliveAllies.add(me);
        for(Region adj : region.getAdjacentRegions())
        {
            if (adj.getProvince().isSC() && this.game.getController(adj.getProvince()) != null){
                Boolean flag = true;
                for (Power ally: alliveAllies){
                    if (ally.isControlling(adj)){
                        flag = false;
                    }
                }
                adjSCEnemy.add(adj);
            }
        }
        return  adjSCEnemy;
    }
    private OrderCommitment makeSupportOrder(Region myLocation,Region destination,Power ally,Region allyLocation){
        MTOOrder order1 = new MTOOrder(me, myLocation, destination);
        Order order2 = new SUPMTOOrder(ally, allyLocation, order1);
        return new OrderCommitment(game.getYear(), game.getPhase(), order2);

    }

        /**
         * Each round, after each power has submitted its orders, this method is called several times:
         * once for each order submitted by any other power.
         *
         *
         * @param orderSubmittedByOtherPlayer An order submitted by any of the other powers.
         */
        @Override
        public void receivedOrder(Order arg0) {
            // TODO Auto-generated method stub

        }

}
