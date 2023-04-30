package com.royale.game;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
//The Dumb Version(tm)
public class card_logic_test extends Thread
{
    static List<card> player1_hand;
    static List<card> player2_hand;
    static List<card> player3_hand;
    static List<card> player4_hand;
    static List<card> hand_return;
    static List<card> swap_return;
    static List<card> player_swap;
    static logic game_logic;

    static List<card> discard_deck;
    static card top;
    static int discard_size;
    static boolean isRevolution;
    public static void main(String[] args)
    {
        game_logic = new logic("test");
        game_logic.deck.shuffle();
	    game_logic.deck.deal_hand(game_logic.player1, 0);
	    game_logic.deck.deal_hand(game_logic.player2, 1);
	    game_logic.deck.deal_hand(game_logic.player3, 2);
	    game_logic.deck.deal_hand(game_logic.player4, 3);  

        Scanner input = new Scanner(System.in);
        isRevolution = false;
        player1_hand = new LinkedList<card>();
        player2_hand = new LinkedList<card>();
        player3_hand = new LinkedList<card>();
        player4_hand = new LinkedList<card>(); 
        discard_deck = new LinkedList<card>();
        player_swap = new LinkedList<card>();
        for(card card: game_logic.player1.player_hand)
        {
            player1_hand.add(card);
            game_logic.player1.hand.add(card);
        }
        for(card card: game_logic.player2.player_hand)
        {
            player2_hand.add(card);
            game_logic.player2.hand.add(card);
        }
        for(card card: game_logic.player3.player_hand)
        {
            player3_hand.add(card);
            game_logic.player3.hand.add(card);
        }
        for(card card: game_logic.player4.player_hand)
        {
            player4_hand.add(card);
            game_logic.player4.hand.add(card);
        }
        System.out.println("\nTycoon Card Test");
        System.out.println("=============================================");

        /*setTycoon(game_logic.player1);
		setRich(game_logic.player2);
		setPoor(game_logic.player3);
		setBeggar(game_logic.player4);
        System.out.println("Role: " + game_logic.player1.role);
        for(card card : player1_hand)
        {
           System.out.print(card.suit + " " + card.value + "(" + card.power + ")" + " || ");
        }
        System.out.print("\nPlease enter 1st card to swap: ");
        String card_input = input.nextLine();
        game_logic.player1.hand = card_swap(card_input);

        System.out.print("Please enter 2nd card to swap: ");
        card_input = input.nextLine();
        game_logic.player1.hand = card_swap(card_input);
        swap();*/

        System.out.print("How many cards do you want to discard?: ");
        String parseInput = input.nextLine();
        discard_size = Integer.parseInt(parseInput);
        System.out.println();
            /*if(!discard_deck.isEmpty())
            {
                System.out.print("\n\nDiscard Deck: ");
                for(card card : discard_deck)
                {
                    System.out.print(card.suit + " " + card.value + "(" + card.power + ")" + " ");
                }
            }
            System.out.println("\n\n" + game_logic.player2.name + "\'s hand: " + player2_hand.size() + "(after)");
            for(card card : player2_hand)
            {  
                System.out.print(card.suit + "-" + card.value + " ");
            }
            System.out.println("\n=============================================");*/
        new Thread(){
            @Override public void run() { round(); }
        }.start();
        new Thread(){
            @Override public void run() { input(); }
        }.start();
    }
    static List<card> discard(String input)
    {
        List<card> returnArr = new LinkedList<card>();
        for(card card : player1_hand)
        {
            if(!input.equals(card.suit + " " + card.value))
            {
                returnArr.add(card);
            }
            else
            {
                discard_deck.add(card);
                top = card;
            }
        }
        return returnArr;
    }
    static List<card> player_turn(List<card> player_hand)
    {
        //check top - does the hand have anything higher? -- how many cards are in play? -> choose according to these metrics
        //how many cards?
        List<card> viable = new LinkedList<card>();
        //deal with start of chain
        if(discard_deck.isEmpty())
        {
            Random rnd = ThreadLocalRandom.current();
            int index = rnd.nextInt(player_hand.size()-1);
            //int played = rnd.nextInt(4);
            viable.add(player_hand.get(index));
            player_hand = discard(player_hand, viable);
            return player_hand;
        }
        if(isRevolution) //revolution
        {
            //what is top? --> what do I have, do I have anything stronger?
            for(card card : player_hand)
            {
                if(card.power < top.power)
                {
                    //consider playing
                    viable.add(card);
                }
            }
            //deal with discards bigger than one card
            if(discard_size > 1)
            {
                
            }
            //skip, if no
            if(viable.isEmpty())
            {
                return player_hand;
            }
            else
            {
                //discard, if yes
                player_hand = discard(player_hand, viable);
            }
        }
        else
        {
            //what is top? --> what do I have, do I have anything stronger?
            for(card card : player_hand)
            {
                if(top.power == 14 && card.value == 3 && card.suit.equals("spades"))
                {
                    viable.add(card);
                    break;
                }
                else if(card.power > top.power)
                {
                    //consider playing
                    viable.add(card);
                }
            }
            card compare_prev = viable.get(0);
            card compare_next = viable.get(0);
            List<card> toDiscard = new LinkedList<card>();
            //deal with discards bigger than one card
            if(discard_size > 1)
            {
                for(int i = 0; i < viable.size(); i++)
                {
                    compare_next = viable.get(i);
                    System.out.println("\n" + compare_prev.suit + "-" + compare_prev.value + " == " + compare_next.suit + "-" + compare_next.value);
                    if(i == 0)
                    {
                        continue;
                    }
                    else if(compare_next.power == compare_prev.power)
                    {
                        if(toDiscard.size() == discard_size)
                        {
                            break;
                        }
                        
                        if(!toDiscard.contains(compare_prev))
                        {
                            toDiscard.add(compare_prev);
                            System.out.println("Added " + compare_prev.suit + "-" + compare_prev.value);
                        }
                        toDiscard.add(compare_next);
                        System.out.println("Added " + compare_next.suit + "-" + compare_next.value);
                    }
                    compare_prev = compare_next;
                }
                viable = toDiscard;
            }
            //skip, if no
            if(viable.isEmpty())
            {
                return player_hand;
            }
            else
            {
                //discard, if yes
                player_hand = discard(player_hand, viable);
            }
        }
        return player_hand;
    }
    static List<card> discard(List<card> player_hand, List<card> input)
    {
        //pick card that matched criteria -> right power (rev/no rev), right num of cards, etc. -> pass into discard (no logic, only removing card)
        discard_deck.clear();
        hand_return = new LinkedList<card>();
        Random rnd = ThreadLocalRandom.current();
        int index = rnd.nextInt(input.size()-1);
        int remove_index = 0;

        if(discard_size == 1)
        {
            discard_deck.add(input.get(index));
            for(card card : player_hand)
            {
                if(input.get(index) == card)
                {
                    break;
                }
                remove_index++;
            }
            top = player_hand.get(remove_index);
            player_hand.remove(remove_index);
        }
        else
        {
            for(card card : input)
            {
                player_hand.remove(card);
            }
        }
        hand_return = player_hand;
        return hand_return;
    }
    static List<card> card_swap(List<card> player_hand, int discard_size, String role)
    {
        List<card> swap = new LinkedList<card>();
        for(int i = 0; i < discard_size; i++)
        {
            Random rnd = ThreadLocalRandom.current();
            int index;
            if(role.equals("tycoon") || role.equals("rich")) // -> lowerbound
            {
                index = rnd.nextInt(6);
                //System.out.println("Index:" + index);
                swap.add(player_hand.get(index));
                player_hand.remove(index);
            }
            else //beggar || poor -> upperbound
            {
                index = rnd.nextInt(13)/2 + 6;
                //System.out.println("Index:" + index);
                swap.add(player_hand.get(index));
                player_hand.remove(index);
            }
        }
        return swap;
    }
    static public List<card> card_swap(String input)
	{
		List<card> returnArr = new LinkedList<card>();
        for(card card : game_logic.player1.hand)
        {
        	if(!input.equals(card.suit + " " + card.value))
        	{
        	    returnArr.add(card);
        	}
        	else
        	{
        	    player_swap.add(card);
        	}
       	}
        return returnArr;
	}
	static public void swap()
	{
		List<card> tycoon_swap = new LinkedList<card>();
		List<card> rich_swap = new LinkedList<card>();
		List<card> poor_swap = new LinkedList<card>();
		List<card> beggar_swap = new LinkedList<card>();
		for(player player : game_logic.players)
		{
			if(player == game_logic.player1)
			{
				switch(player.role)
				{
					case "beggar": //give 2 'strong' cards to tycoon
					{
						for(card card : player_swap)
                        {
                            tycoon_swap.add(card);
                        }
						break;
					}
					case "poor": //give 1 'strong' card to rich
					{
						for(card card : player_swap)
                        {
                            rich_swap.add(card);
                        }
						break;
					}
					case "rich": //give 1 'weak' card to poor
					{
						for(card card : player_swap)
                        {
                            poor_swap.add(card);
                        }
						break;
					}
					case "tycoon": //give 2 'weak' cards to beggar
					{
						for(card card : player_swap)
                        {
                            beggar_swap.add(card);
                        }
						break;
					}
				}
				continue;
			}
			switch(player.role)
			{
				case "beggar": //give 2 'strong' cards to tycoon
				{
					tycoon_swap = card_swap(player.hand, 2, player.role);
					break;
				}
				case "poor": //give 1 'strong' card to rich
				{
					rich_swap = card_swap(player.hand, 1, player.role);
					break;
				}
				case "rich": //give 1 'weak' card to poor
				{
					poor_swap = card_swap(player.hand, 1, player.role);
					break;
				}
				case "tycoon": //give 2 'weak' cards to beggar
				{
					beggar_swap = card_swap(player.hand, 2, player.role);
					break;
				}
			}

		}
		for(player player : game_logic.players)
		{
			switch(player.role)
			{
				case "beggar": //give 2 'strong' cards to tycoon
				{
                    for(card card : beggar_swap)
                    {
                        player.hand.add(card);
                    }
					break;
				}
				case "poor": //give 1 'strong' card to rich
				{
					for(card card : poor_swap)
                    {
                        player.hand.add(card);
                    }
					break;
				}
				case "rich": //give 1 'weak' card to poor
				{
					for(card card : rich_swap)
                    {
                        player.hand.add(card);
                    }
					break;
				}
				case "tycoon": //give 2 'weak' cards to beggar
				{
					for(card card : tycoon_swap)
                    {
                        player.hand.add(card);
                    }
					break;
				}
			}
		}
        System.out.print("Tycoon:");
        for(card card : tycoon_swap)
        {
           System.out.print(card.suit + " " + card.value + "(" + card.power + ")" + " || ");
        }
        System.out.print("\nRich:");
        for(card card : rich_swap)
        {
           System.out.print(card.suit + " " + card.value + "(" + card.power + ")" + " || ");
        }
        System.out.print("\nPoor:");
        for(card card : poor_swap)
        {
           System.out.print(card.suit + " " + card.value + "(" + card.power + ")" + " || ");
        }
        System.out.print("\nBeggar:");
        for(card card : beggar_swap)
        {
           System.out.print(card.suit + " " + card.value + "(" + card.power + ")" + " || ");
        }
        System.out.println("");
		
	}
    static void setTycoon(player player)
	{
		player.role = "tycoon";
	}
	static void setRich(player player)
	{
		player.role = "rich";
	}
	static void setPoor(player player)
	{
		player.role = "poor";
	}
	static void setBeggar(player player)
	{
		player.role = "beggar";
	}
    static void player_turns()
    {
        game_logic.player2.hand = player_turn(game_logic.player2.hand);
        game_logic.player3.hand = player_turn(game_logic.player3.hand);
        game_logic.player4.hand = player_turn(game_logic.player4.hand);
    }
    synchronized static public void input()
    {
        Scanner input = new Scanner(System.in);
        for(card card : player1_hand)
        {
            System.out.print(card.suit + " " + card.value + "(" + card.power + ")" + " || ");
        }
        if(!discard_deck.isEmpty())
        {
            System.out.print("\n\nDiscard Deck: ");
            for(card card : discard_deck)
            {
                System.out.print(card.suit + " " + card.value + "(" + card.power + ")" + " ");
            }
        }
        System.out.println();
        /* if(discard_size == 0)
        {
            System.out.print("End Test? (y/n): ");
            String exit = input.nextLine();
            if(exit.equals("y"))
            {
                System.out.println("Test ended.");
                input.close();
                break;
            }
        } */
        if(discard_size == 1)
        {
            System.out.print("Please enter a card to discard (i.e. diamonds 3): ");
            String card_input = input.nextLine();
            card temp = new card();
            for(card card : player1_hand)
            {
                if(card_input.equals(card.suit + " " + card.value))
                {
                    temp = card;
                }
            }
            player1_hand = discard(card_input);
        }
        else
        {
            System.out.print("Please enter a card to discard: ");
            String card_input = input.nextLine();
            int firstCard = Integer.parseInt(card_input.substring(card_input.indexOf(" ") + 1));
            card temp = new card();
            for(card card : player1_hand)
            {
                if(card_input.equals(card.suit + " " + card.value))
                {
                    temp = card;
                }
            }
            player1_hand = discard(card_input);
            //System.out.println(firstCard);
            for(int i = 0; i < (discard_size - 1); i++)
            {
                System.out.print("Please enter a card to discard: ");
                card_input = input.nextLine();
                int compare = Integer.parseInt(card_input.substring(card_input.indexOf(" ") + 1));
                //System.out.println(compare);
                if(compare == firstCard || firstCard == 14 || compare == 14)
                {
                    player1_hand = discard(card_input);
                }
                else
                {
                    System.out.println("Please enter cards with matching values.");
                    break;
                }
            }
            if(discard_size == 4)
            {
                System.out.println("REVOLT!! -- CARD STRENGTH REVERSED");
            }
        }
        //notify();
    }
    synchronized static public void round()
    {
        boolean end = false;
        while(!end)
        {

        }
    }
}
