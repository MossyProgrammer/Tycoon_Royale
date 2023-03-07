package com.royale.game;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Input.TextInputListener;
//import com.badlogic.gdx.graphics.Texture;

public class back_end_logic
{
    deck new_deck;
    player player1;
    player player2;
    player player3;
    player player4;
    public static void main(String [] args) {}
    public void startGame()
    {
        String name_input = "Sephiroth";
        //add input listener -- temp name for now
        player1 = new player(name_input); //take input from player
        player2 = new player("Maruki"); //AI
        player3 = new player("Lea"); //AI
        player4 = new player("Ryuji"); //AI

        new_deck = new deck(player1, player2, player3, player4);
        new_deck.setDeck();
    }
}
//===============================================================================
class cardHandler
{
    //cards are assigned in an array with 2 main values, 1 of which aren't used in most situations
    //3,4,5,6,7,8,9,10 Jack(11), Queen(12), King(13), Ace(1/15), 2/16 - int values
    //Suits - stored in char - C (Clubs) - S (Spades)- D (Diamonds) - H (Hearts) - J (Joker)
    int value;
    char suit;
    cardHandler(int inputValue, char inputSuit)
    {
        value = inputValue;
        suit = inputSuit;
    }
    cardHandler(){}
    void setValue(int inputValue)
    {
        value = inputValue;
    }
    void setSuit(char inputSuit)
    {
        suit = inputSuit;
    }
}
//===============================================================================
class player //player class, deals with both player and ai
{
    cardHandler[] card = new cardHandler[14]; //14 cards per hand (4 players - 56 cards)
    int num_of_cards;

    int points; //highest total in end wins
    String name;
    boolean skip; //links to button to skip turn if unable to play?
    int role; //0:beggar -- 1:poor -- 2:rich -- 3:tycoon && 4:commoner

    player(String nameInput)
    {
        name = nameInput;
        points = 0;
        num_of_cards = 14;
        role = 4;
    }
    cardHandler[] discard() //to center pile (needs work)
    {
        cardHandler selected1 = new cardHandler();
        cardHandler selected2 = new cardHandler();
        cardHandler selected3 = new cardHandler();
        cardHandler selected4 = new cardHandler();
        cardHandler[] discarded_cards = new cardHandler[4];
        //input things go here to decide how many and what cards get discarded

        int num_discarded = 3; //I don't care if that's an error right now, lol
        //remove cards from hand
        num_of_cards -= num_discarded;
        switch(num_discarded)
        {
            case 1:
            {
                //selected1 = input stuff
                discarded_cards[0] = selected1;
            }
            case 2:
            {
                //selected1 = input stuff
                //selected1 = input stuff
                discarded_cards[0] = selected1;
                discarded_cards[1] = selected2;
            }
            case 3:
            {
                //selected1 = input stuff
                //selected2 = input stuff
                //selected3 = input stuff
                discarded_cards[0] = selected1;
                discarded_cards[1] = selected2;
                discarded_cards[2] = selected3;
            }
            case 4:
            {
                //selected1 = input stuff
                //selected2 = input stuff
                //selected3 = input stuff
                //selected4 = input stuff
                discarded_cards[0] = selected1;
                discarded_cards[1] = selected2;
                discarded_cards[2] = selected3;
                discarded_cards[3] = selected4;
            }
            default:
            {
                //uh oh, stinky...
                break;
            }
        }
        return discarded_cards;
    }
}
//===============================================================================
class deck
{   
    cardHandler[] fullDeck = new cardHandler[56];
    cardHandler[] currentDeck = new cardHandler[56];

    player player1;
    player player2;
    player player3;
    player player4;

    deck(player temp1, player temp2, player temp3, player temp4)
    {
        player1 = temp1;
        player2 = temp2;
        player3 = temp3;
        player4 = temp4;
    }
    void setDeck() //intializes the full deck (asset, suit, and value)
    {
        for(int i = 0; i < 4; i++)
        {
            for(int k = 0; k < 13; k++)
            {
                //foreach suit
                cardHandler new_card = new cardHandler();
                switch(i)
                {
                    case 0: //set clubs
                    {
                        new_card.setSuit('C');
                        break;
                    }
                    case 1: //set spades
                    {
                        new_card.setSuit('S');
                        break;
                    }
                    case 2: //set diamonds
                    {
                        new_card.setSuit('D');
                        break;
                    }
                    case 3: //set hearts
                    {
                        new_card.setSuit('H');
                        break;
                    }
                    default:
                    {
                        //something really broke...
                        return;
                    }
                }
                //foreach card
                new_card.setValue(k + 1);
                fullDeck[k + (13*i)] = new_card;
            }
        }
        //set jokers
        fullDeck[52] = new cardHandler(14, 'J');
        fullDeck[53] = new cardHandler(14, 'J');
        fullDeck[54] = new cardHandler(14, 'J');
        fullDeck[55] = new cardHandler(14, 'J');
        for(int l = 0; l < 56; l++)
        {
            currentDeck[l] = fullDeck[l];
        }
    }
    void shuffle() //shuffles deck before new hand is dealt
    {
        Random rnd = ThreadLocalRandom.current();
        for (int i = currentDeck.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            cardHandler tempCard = currentDeck[index];
            currentDeck[index] = currentDeck[i];
            currentDeck[i] = tempCard;
        }
    }
    void dealHand(player temp, int iteration) //deal cards to hand
    {
        //remember to adjust full deck to make sure they aren't pulling all the same cards
        for(int i = 0; i < 14; i++)
        {
            temp.card[i] = currentDeck[i+(14*iteration)];
        }
        //sort, so they are in order (numerically, ascending)
        for( int j = 0; j < temp.card.length - 1; j++)
	    {
		    int minIndex = j;
		    for ( int k = j + 1; k < temp.card.length; k++)
		    {
			    if (temp.card[k].value < temp.card[minIndex].value)
			    {
				    minIndex = k;
			    }
		    }
		    cardHandler swap = temp.card[j];
		    temp.card[j] = temp.card[minIndex];
		    temp.card[minIndex] = swap;
	    }
    }
}
//===============================================================================
