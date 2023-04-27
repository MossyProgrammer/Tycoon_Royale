package com.royale.game;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

//./gradlew html:superDev
public class Game_Display implements Screen
{
    final royale game;
    back_end_logic game_logic = new back_end_logic();

    Sound cardDealt;
	Music background_music;
	OrthographicCamera camera;
	SpriteBatch batch;
	Texture ui;
    BitmapFont font;
	ShapeRenderer shape;

	//add reference
	Texture background = new Texture(Gdx.files.internal("tabletop.jpeg"));

	//card size - 88px x 124px -- from: add reference
	Texture cardBack = new Texture(Gdx.files.internal("Card_Back-88x124.png"));
	Texture cardBack_iso = new Texture(Gdx.files.internal("Card_Back_Vertical-88x168.png"));
	Texture spades = new Texture(Gdx.files.internal("Spades-88x124.png"));
	Texture clubs = new Texture(Gdx.files.internal("Clubs-88x124.png"));
	Texture hearts = new Texture(Gdx.files.internal("Hearts-88x124.png"));
	Texture diamonds = new Texture(Gdx.files.internal("Diamonds-88x124.png"));

	Rectangle[] hand;
	Rectangle[] opponent_hand;

	//Texture test_asset = new Texture(Gdx.files.internal("test_asset.jpg"));
	//draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) -- Draws a portion of the texture -- Good for the cards

	public Game_Display(final royale input)
    {
        this.game = input;
		//static textures
		//cardBack = new Texture(Gdx.files.internal(""));

		//background music
		//music.setLooping(true)
		//sound effects
		camera = new OrthographicCamera();
		camera.setToOrtho(false,800, 480); //adjust later
		//Is the viewpoint thing a issue with how I am using it?


		//start game logic - other internal things(shuffle/dealing hands/rounds/chains/etc.)
		game_logic.startGame();
		//test(game_logic);
		game_logic.new_deck.shuffle();
		game_logic.new_deck.dealHand(game_logic.player1, 0);
    }
	@Override
    public void render(float delta)
	{
		ScreenUtils.clear(0,0,0, 0);
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.shape.begin(ShapeType.Line);


		game.batch.draw(background, 0, 0);
		game.shape.rect(260, 180, 88, 124); // adjust location later
		//test_render(game_logic);
		render_card(game_logic.player1);
		
		
		game.batch.end();
		game.shape.end();
		//dispose();
    }
	@Override
	public void show()
	{
		//background music.play();
	}
	@Override
	public void dispose()
	{
		//dispose of textures
		ui.dispose();
		background.dispose();
		cardBack.dispose();
		cardBack_iso.dispose();
		spades.dispose();
		clubs.dispose();
		hearts.dispose();
		diamonds.dispose();
		//texture.dispose();
	}
//===============================================================================
	void test(back_end_logic game_logic)
	{
		String card = game_logic.player1.card[1].suit + " " + game_logic.player1.card[1].value;
		System.out.println(card);
	}
	void test_render(back_end_logic game_logic)
	{
		String card = game_logic.player1.card[1].suit + " " + game_logic.player1.card[1].value;
		//game.batch.draw(spades, 120, 20, 0, 0, 88, 124);
		if(card.startsWith("S"))
		{
			game.batch.draw(spades, 120, 20, 0, 0, 88, 124);
		}
		else if(card.startsWith("H"))
		{
			game.batch.draw(hearts, 120, 20, 0, 0, 88, 124);
		}
		else if(card.startsWith("D"))
		{
			game.batch.draw(diamonds, 120, 20, 0, 0, 88, 124);
		}
		else
		{
			game.batch.draw(clubs, 120, 20, 0, 0, 88, 124);
		}
		//game.font.draw(batch, card, 100, 100);
	}
	public void round(back_end_logic game_logic)
	{
		//variables:
		cardHandler[][] discardDeck = new cardHandler[16][4]; //deals with discard single, doubles, triples, and rev. - shouldn't go over 16, hopefully?
		cardHandler discard;
    	cardHandler currentCard;

		boolean end_of_chain = false;
    	boolean end_of_round = false;
    	boolean isRevolution = false;

		player[] order = new player[4];
		player p1 = game_logic.player1;
		player p2 = game_logic.player2;
		player p3 = game_logic.player3;
		player p4 = game_logic.player4;
		player[] players = {p1, p2, p3, p4};
		
		//start round
		game_logic.new_deck.shuffle();
		game_logic.new_deck.dealHand(p1, 0);
		game_logic.new_deck.dealHand(p2, 1);
		game_logic.new_deck.dealHand(p3, 2);
		game_logic.new_deck.dealHand(p4, 3);

		while(!end_of_round)
		{
			//renderHand();
			//determine order
			roundOrder(p1, p2, p3, p4, order, players);
			//start chain
			while(!end_of_chain)
			{
				if(isRevolution)
				{
					end_of_chain = revolution_cardChain();
				}
				else
				{
					end_of_chain = cardChain();
				}
				//if current card > discard || current card == 8 -> end chain
				//else continue
			}
			//discard - center deck
			//repeat until tycoon-rich-poor-beggar have been decided then end this round
		}
		//assign points
		//return and go to next round
	}
	public void render_card(player player)
	{
		//for each card in Player hand -- render iteratively (create hitboxes for picking cards?)
		int value;
		char suit;
		int iter = 0; //for rendering hand
		for(cardHandler card : player.card)
		{
			Texture tempTexture = new Texture(Gdx.files.internal("test_asset.jpg"));
			suit = card.suit;
			value = card.value;
			//deal with selecting correct texture
			switch(suit)
			{
				case 'D':
				{
					tempTexture = diamonds;
					break;
				}
				case 'H':
				{
					tempTexture = hearts;
					break;
				}
				case 'C':
				{
					tempTexture = clubs;
					break;
				}
				case 'S':
				{
					tempTexture = spades;
					break;
				}
				default:
				{
					//uh oh error
					break;
				}
			}
			//texture -- screen x, screen y, file x, file y, width(88), height(124)
			//select appropriate coords from card value 0(ace)-12(king) // 14 for Jokers
			//game.batch.begin();
			if(value < 5) //0-4 (1-5)
			{
				game.batch.draw(tempTexture, (20 + (iter*90)), 20, (0 + (value*88)), 0, 88, 124);
			}
			else if(value > 5 || value < 10) //5-9 (6-10)
			{
				game.batch.draw(tempTexture, (20 + (iter*90)), 20, (0 + (value*88)), 124, 88, 124);
			}
			else if(value > 10 && value != 14) //10-12 (JQK)
			{
				game.batch.draw(tempTexture, (20 + (iter*90)), 20, (0 + (value*88)), 248, 88, 124);
			}
			else //joker - different file
			{
				game.batch.draw(cardBack, (20 + (iter*90)), 20); //placeholder
				//tempTexture = new Texture(Gdx.files.internal("joker.jpg"));
				//game.batch.draw(tempTexture, 20, 20, 0, 0, 88, 124);
			}
			//game.batch.end();
			iter++;
		}
	}
	public void renderOpponentHands()
	{
		//render num of cards in opponent's hands in iso/normal card back
		//game.batch.draw(cardBack, 20, 20, 0, 0, 88, 124);
		//game.batch.draw(spades, 120, 20, 0, 0, 88, 124);
	}
	public void renderDiscardDeck()
	{
		//render as card is discarded - goes into center box
		//game.batch.draw(spades, 120, 20, 0, 0, 88, 124);
	}
	boolean cardChain()
	{
		//skip condition
		boolean beat;
		return true;

	}
	boolean revolution_cardChain()
	{
		//reverse of cardChain()
		return true;
	}
	void roundOrder(player p1, player p2, player p3, player p4, player[] order, player[] players)
	{
		if(p1.role == 4)
		{
			//commoner round
			order[0] = p1;
        	order[1] = p2; 
        	order[2] = p3;
    		order[3] = p4;
			return;
		}
		for (player player : players)
		{
			switch(player.role)
			{
				case 0: //isBeggar
				{
					order[3] = player;
					break;
				}
				case 1: //isPoor
				{
					order[2] = player;
					break;
				}
				case 2: //isRich
				{
					order[1] = player;
					break;
				}
				case 3: //isTycoon
				{
					order[0] = player;
					break;
				}
				default:
				{
					//error?
					break;
				}
			}
		}
	}
    //unused - but needed to implement Screen
//===============================================================================
    @Override
	public void resize(int width, int height){}
	@Override
	public void hide(){}
	@Override
	public void pause(){}
	@Override
	public void resume(){}
//===============================================================================
}
