package com.royale.game;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class logic 
{
    deck deck;
    player[] players = new player[4];
    String[] roles = {"beggar", "poor", "rich", "tycoon"};

    player player1;
    player player2 = new player("Ryuji");
    player player3 = new player("Lea");
    player player4 = new player("Maruki");

    logic(String name_input)
    {
        player1 = new player(name_input);
        deck = new deck();
        players[0] = player1;
        players[1] = player2;
        players[2] = player3;
        players[3] = player4;
    }
}
class card
{
    String suit;
    int value;
    int power;
    card(){}
    card(int input_value)
    {
        value = input_value;
    }
    card(String input_suit, int input_value)
    {
        suit = input_suit;
        value = input_value;
    }
    card(String input_suit, int input_value, int input_power)
    {
        suit = input_suit;
        value = input_value;
        power = input_power;
    }
    void setPower(card card)
    {
        if(card.value > 2 && card.value <= 12) //3 -> K
        {
            card.power = card.value;
        }
        else if(card.value == 1) // A -> 12
        {
            card.power = 12;
        }
        else if(card.value == 2)
        {
            card.power = 13;
        }
    }
}
class deck
{
    card[] deck = new card[56];
    String[] suits = {"clubs", "spades", "diamonds", "hearts"};
    int[] values = {1,2,3,4,5,6,7,8,9,10,11,12,13}; //14 = joker

    deck()
    {
        setDeck();
    }
    void setDeck()
    {
        for(int i = 0; i < suits.length; i++)
        {
            for(int j = 0; j < values.length; j++)
            {
                deck[j + (13*i)] = new card(suits[i], values[j]);
                if(j >= 2 && j <= 12) //3 -> K
                {
                    deck[j + (13*i)].power = values[j] - 2;
                }
                else if(j == 0) //A
                {
                    deck[j + (13*i)].power = 12;
                }
                else if(j == 1) //2
                {
                    deck[j + (13*i)].power = 13;
                }
            }
        }
        deck[52] = new card("joker", 14, 14);
        deck[53] = new card("joker", 14, 14);
        deck[54] = new card("joker", 14, 14);
        deck[55] = new card("joker", 14, 14);
    }
    void shuffle()
    {
        Random rnd = ThreadLocalRandom.current();
        for (int i = deck.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            card tempCard = deck[index];
            deck[index] = deck[i];
            deck[i] = tempCard;
        }
    }
    void deal_hand(player temp, int iteration)
    {
        //remember to adjust full deck to make sure they aren't pulling all the same cards
        for(int i = 0; i < 14; i++)
        {
            temp.player_hand[i] = deck[i+(14*iteration)];
        }
        //sort, so they are in order (numerically, ascending)
        for( int j = 0; j < temp.player_hand.length - 1; j++)
	    {
		    int minIndex = j;
		    for ( int k = j + 1; k < temp.player_hand.length; k++)
		    {
                //if (temp.player_hand[k].value < temp.player_hand[minIndex].value)
			    if (temp.player_hand[k].power < temp.player_hand[minIndex].power)
			    {
				    minIndex = k;
			    }
		    }
		    card swap = temp.player_hand[j];
		    temp.player_hand[j] = temp.player_hand[minIndex];
		    temp.player_hand[minIndex] = swap;
	    }
    }
}
class player
{
    String name;
    String role;
    int points;
    card[] player_hand = new card[14];
    List<card> hand = new LinkedList<card>();

    player(String input)
    {
        name = input;
        points = 0;
        role = "commoner";
    }
}

/*class opponent_ai
{
    List<card> hand_return;
    List<card> swap_return;
    List<card> player2_hand;
    List<card> player3_hand;
	List<card> player4_hand;
    logic game_logic;
 
    opponent_ai(List<card> input_p2,List<card> input_p3,List<card> input_p4, logic game_logic_input)
    {
        player2_hand = input_p2;
        player3_hand = input_p3;
        player4_hand = input_p4;
        game_logic = game_logic_input;
    }

    List<card> player_turn(List<card> discard_deck, card top, List<card> player_hand)
    {
        //check top - does the hand have anything higher? -- how many cards are in play? -> choose according to these metrics
        //how many cards?
        List<card> viable = new LinkedList<card>();
        if(discard_deck.isEmpty())
        {
            Random rnd = ThreadLocalRandom.current();
            int index = rnd.nextInt(player_hand.size()-1);
            int played = rnd.nextInt(4);
            viable.add(player_hand.get(index));
            discard_deck.clear();
            player_hand = discard(player_hand, discard_deck, top, viable);
            return player_hand;
        }
        if(discard_deck.size() == 4) //revolution
        {
            for(card card : player_hand)
            {
                if(card.power < top.power)
                {
                    //consider playing
                    viable.add(card);
                }
            }
            int compare_prev = viable.get(0).power;
            int compare_next = viable.get(0).power;
            List<card> nonviable = new LinkedList<card>();
            int num_of_duplicates = 0;
            int i = 0;
            for(card card : viable)
            {
                compare_next = viable.get(i).power;
                if(compare_next == compare_prev && num_of_duplicates <= discard_deck.size())
                {
                    num_of_duplicates++;
                }
                else
                {
                    nonviable.add(card);
                }
                compare_prev = compare_next;
            }
            i = 0;
            for(card card : nonviable)
            {
                if(card == viable.get(i))
                {
                    viable.remove(i);
                }
                i++;
            }
            //skip, if no
            if(viable.isEmpty())
            {
                return player_hand;
            }
            else
            {
                //discard_deck.clear();, then discard, if yes
                discard_deck.clear();
                player_hand = discard(player_hand, discard_deck, top, viable);
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
            int compare_prev = viable.get(0).power;
            int compare_next = viable.get(0).power;
            List<card> nonviable = new LinkedList<card>();
            int num_of_duplicates = 0;
            int i = 0;
            for(card card : viable)
            {
                compare_next = viable.get(i).power;
                if(compare_next == compare_prev && num_of_duplicates <= discard_deck.size())
                {
                    num_of_duplicates++;
                }
                else
                {
                   nonviable.add(card);
                }
                compare_prev = compare_next;
            }
            i = 0;
            for(card card : nonviable)
            {
                if(card == viable.get(i))
                {
                    viable.remove(i);
                }
                i++;
            }
            //skip, if no
            if(viable.isEmpty())
            {
                return player_hand;
            }
            else
            {
                //discard_deck.clear();, then discard, if yes
                discard_deck.clear();
                player_hand = discard(player_hand, discard_deck, top, viable);
            }
        }
        return player_hand;
    }
    List<card> discard(List<card> player_hand, List<card> discard_deck, card top, List<card> input)
    {
        //pick card that matched criteria -> right power (rev/no rev), right num of cards, etc. -> pass into discard (no logic, only removing card)
        hand_return = new LinkedList<card>();
        for(card card : player_hand)
        {
            if(input != card)
            {
                hand_return.add(card);
            }
            else
            {
                discard_deck.add(card);
                top = card;
            }
        }
        return hand_return;
    }
    List<card> player_swap(player player, int num_of_cards)
    {
        List<card> swap = new LinkedList<card>();
        //
        return swap;
    }
    List<card> tycoon_swap(List<card> swap)
    {
        for(player player : game_logic.players)
		{
			if(player.role.equals("tycoon"))
			{
                //swap (2) cards
                swap_return = player_swap(player, 2);
			}
		}
        return swap_return;
    }
    List<card> rich_swap(List<card> swap)
    {
        for(player player : game_logic.players)
		{
		    if(player.role.equals("rich"))
			{
                //swap (1) card
                swap_return = player_swap(player, 1);
			}
		}
        return swap_return;
    }
    List<card> poor_swap(List<card> swap)
    {
        for(player player : game_logic.players)
		{
			if(player.role.equals("poor"))
			{
                //swap (1) cards
                swap_return = player_swap(player, 1);
			}
		}
        return swap_return;
    }
    List<card> beggar_swap(List<card> swap)
    {
        for(player player : game_logic.players)
		{
			if(player.role.equals("beggar"))
			{
                //swap (2) cards
                swap_return = player_swap(player, 2);
			}
		}
        return swap_return;
    }
            
    


}*/