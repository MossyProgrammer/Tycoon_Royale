package com.royale.game;
import java.util.*;

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
        Random rnd = new Random();
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
    boolean skipped;

    player(String input)
    {
        name = input;
        points = 0;
        role = "commoner";
        skipped = false;
    }
}