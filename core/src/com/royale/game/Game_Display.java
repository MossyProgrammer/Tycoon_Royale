package com.royale.game;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
//./gradlew html:superDev
public class Game_Display implements Screen
{
    final royale game;
	logic game_logic;

    Sound cardDealt;
	Music background_music;

	OrthographicCamera camera;
	Viewport viewport;
	SpriteBatch batch;
	Texture ui;
    BitmapFont font;

	Skin skin;
	Stage stage;
	Table table;
	Texture background;
	Texture checkbox_ui;
	Label outputLabel;
	Label roundLabel;
	Label currentCard;

	Texture card_back, card_back_rotated, joker, spades, clubs, hearts, diamonds;
	Texture temp;

	ButtonGroup<CheckBox> card_selection;
	Array<CheckBox> checkbox_reset;
	Array<CheckBox> checked;
	TextButton skip;
	TextButton end_turn;
	TextButton card_swap;
	Dialog dialog;

	List<card> discard_deck;
	int discard_size;
	List<card> player_swap;
	List<card> ai_swap;
	List<card> hand_return;
    List<card> swap_return;
	boolean dupe;
	boolean end_of_turn;
	boolean end_of_chain;
	boolean end_of_round;
	boolean isRevolution;
	card top;
	
	public Game_Display(final royale input)
    {
		//intialization
        this.game = input;
		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.internal("skin/expee-ui.json"));
		Gdx.input.setInputProcessor(stage);
		Gdx.graphics.setContinuousRendering(false);
		
		//base card textures from Screaming Brain Studios -- https://screamingbrainstudios.itch.io/poker-pack?download
		card_back = new Texture(Gdx.files.internal("Card_Back-88x124.png"));
		card_back_rotated = new Texture(Gdx.files.internal("card_back_rotated.png"));
		joker = new Texture(Gdx.files.internal("joker-wip.png")); //temp
		spades = new Texture(Gdx.files.internal("Spades-88x124.png"));
		clubs = new Texture(Gdx.files.internal("Clubs-88x124.png"));
		hearts = new Texture(Gdx.files.internal("Hearts-88x124.png"));
		diamonds = new Texture(Gdx.files.internal("Diamonds-88x124.png"));

		//basic, possibly temp background 
		background = new Texture(Gdx.files.internal("tabletop2.png"));
		checkbox_ui = new Texture(Gdx.files.internal("ui_test.png"));

		//set stage and table
		table = new Table();
		table.setFillParent(true);
		stage.addActor(table);
		table.setDebug(true);
		//camera/viewpoint
		camera = new OrthographicCamera(1000, 600);
		camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
		camera.update();

		//background music, if needed
			//music.setLooping(true)
		//sound effects, if needed

		//start game logic - other internal things(shuffle/dealing hands/rounds/chains/etc.)
		game_logic = new logic("Joey");
        setHands(game_logic);
		dupe = false;
		isRevolution = false;
		end_of_chain = false;
		end_of_round = false;
		end_of_turn = false;

		//card selection buttons
		outputLabel = new Label("Select a card:", skin);
		outputLabel.setPosition(400, 20);
		stage.addActor(outputLabel);

		currentCard = new Label("Current Card", skin);
		currentCard.setPosition(600, 20);
		stage.addActor(currentCard);

		card_selection = new ButtonGroup<CheckBox>();
		checkbox_reset = new Array<CheckBox>();
		checked = new Array<CheckBox>();
		card_selection.setMaxCheckCount(4);
		int i = 0;
		for(card card : game_logic.player1.hand)
		{
			CheckBox btn = new CheckBox(card.suit + " " + card.value, skin);
			btn.setPosition((400 + (i * 90)), 40);
			btn.setSize(75, 25);
			btn.setChecked(false);
			card_selection.add(btn);
			checkbox_reset.add(btn);
			stage.addActor(btn);
			i++; 
		}
		card_selection.uncheckAll();
		//skip turn
		skip = new TextButton("Skip Turn", skin);
		skip.setPosition(530, 20);
		skip.addListener(new InputListener(){
			@Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
				{
					//skip turn --> returns discard? moves to next order?
					dialog = new Dialog("Skipped", skin);
					dialog.setHeight(100);
					dialog.setWidth(250);
					dialog.setPosition(375, 250);
					dialog.button("Exit");
					stage.addActor(dialog);
					end_of_turn = true;
					//move to next set of player turns
					player_turns();
					return true;
				}
			});
		stage.addActor(skip);
		//submission/end turn
		end_turn = new TextButton("Submit", skin);
		end_turn.setPosition(480, 20);
		end_turn.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
			{
				checked = card_selection.getAllChecked();
				List<String> compare = new LinkedList<String>();
				List<Integer> discarded = new LinkedList<Integer>();
				int i = 0;
				if(checked.size == 0)
				{
					dialog = new Dialog("Invalid Entry", skin);
					dialog.setHeight(100);
					dialog.setWidth(250);
					dialog.setPosition(375, 250);
					dialog.text("Please select at least 1 card");
					dialog.button("Exit");
					stage.addActor(dialog);
					return false;
				}
				for(CheckBox box : checked) //checkbox -> String
				{
					//Label:   {label} - start at index 8 to get rid of leading 'Label:' label
					compare.add(box.getLabel().toString().substring(7)); 
					i++;
				}
				i = 0;
				int compare_prev = Integer.parseInt(compare.get(0).substring(compare.get(0).indexOf(" ")+ 1));
				int compare_next = Integer.parseInt(compare.get(0).substring(compare.get(0).indexOf(" ")+ 1));
				if(compare.size() != 1)
				{
					for(String temp : compare) //parse int/compare
					{
						compare_next = Integer.parseInt(compare.get(i).substring(compare.get(i).indexOf(" ")+ 1));
						discarded.add(compare_prev);
						if(compare_prev != compare_next && compare_prev != 14 && compare_next != 14)
						{
							//System.out.println("please enter matching cards");
							//enter dialog box
							dialog = new Dialog("Invalid Entry", skin);
							dialog.setHeight(100);
							dialog.setWidth(250);
							dialog.setPosition(375, 250);
							dialog.text("Please Enter Matching Cards");
							dialog.button("Exit");
							stage.addActor(dialog);
							card_selection.uncheckAll();
							return false;
						}
						compare_prev = compare_next;
						i++;
					}
				}
				if(!checkPower(compare) && !isRevolution)
				{
					return false;
				}
				else if(isRevolution && !check_revoltPower(compare))
				{
					return false;
				}
				//pass to discard
				for(String temp : compare)
				{
					game_logic.player1.hand = discard(temp);
				}
				discard_size = compare.size();
				currentCard.setText(top.suit + " " + top.value);
				//remove existing checkboxes
				for(CheckBox box : checked)
				{
					box.setVisible(false);
				}
				card_selection.uncheckAll();
				
				//next person request -> move to next in order
				Gdx.graphics.requestRendering();
				end_of_turn = true;
				player_turns();
				currentCard.setText(top.suit + " " + top.value);
				Gdx.graphics.requestRendering();
                return true;
            }
        });
		stage.addActor(end_turn);
		Gdx.graphics.requestRendering();
		//setTycoon(game_logic.player1);
		//setRich(game_logic.player2);
		//setPoor(game_logic.player3);
		//setBeggar(game_logic.player4);
		//card swap goes here -- first 'submit' goes to swapping
		if(!game_logic.player1.role.equals("commoner")) //!game_logic....equals() (testing purposes its currently ==)
		{
			end_turn.setVisible(false);
			skip.setVisible(false);

			card_swap_dialog();
			card_swap = new TextButton("Swap", skin);
			card_swap.setPosition(480, 20);
			card_swap.addListener(new InputListener(){
            	@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
				{
					checked = card_selection.getAllChecked();
					List<String> compare = new LinkedList<String>();
					if(checked.size == 0)
					{
						dialog = new Dialog("Invalid Entry", skin);
						dialog.setHeight(100);
						dialog.setWidth(250);
						dialog.setPosition(375, 250);
						dialog.text("Please select at least 1 card");
						dialog.button("Exit");
						stage.addActor(dialog);
						return false;
					}
					else if(checked.size > 1 && (game_logic.player1.role.equals("poor") || game_logic.player1.role.equals("rich")))
					{
						dialog = new Dialog("Invalid Entry", skin);
						dialog.setHeight(100);
						dialog.setWidth(250);
						dialog.setPosition(375, 250);
						dialog.text("Too many cards selected, please unselect.");
						dialog.button("Exit");
						stage.addActor(dialog);
						return false;
					}
					else if(checked.size > 2 && (game_logic.player1.role.equals("beggar") || game_logic.player1.role.equals("tycoon")))
					{
						dialog = new Dialog("Invalid Entry", skin);
						dialog.setHeight(100);
						dialog.setWidth(250);
						dialog.setPosition(375, 250);
						dialog.text("Too many cards selected, please unselect.");
						dialog.button("Exit");
						stage.addActor(dialog);
						return false;
					}
					for(CheckBox box : checked)
					{
						compare.add(box.getLabel().toString().substring(7));
					}
					for(String temp : compare)
					{
						game_logic.player1.hand = card_swap(temp);
					}
					swap();
					//reset checkboxes
					int i = 0;
					for(CheckBox btn : checkbox_reset)
					{
						btn.setText(game_logic.player1.hand.get(i).suit + " " + game_logic.player1.hand.get(i).value);
						i++;
					}
					card_selection.uncheckAll();

					card_swap.setVisible(false);
					end_turn.setVisible(true);
					skip.setVisible(true);
					Gdx.graphics.requestRendering();
    	            return true;
            	}
        	});
			stage.addActor(card_swap);
			Gdx.graphics.requestRendering();
		}


		//start the round
		/*dialog = new Dialog("Starting Round 1", skin);
		dialog.setHeight(50);
		dialog.setWidth(250);
		dialog.setPosition(375, 250);
		dialog.button("Begin");
		stage.addActor(dialog);*/

		//round(game_logic, round_number);
		//continue for rounds 2 & 3 - reset hands and clear discard 
		//at end, total points and display winner
		Gdx.graphics.requestRendering();

    }
	@Override
    public void render(float delta)
	{
		ScreenUtils.clear(0,0,0, 0);
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		int screenWidth = Gdx.graphics.getWidth();
		int screenHeight = Gdx.graphics.getHeight();
		HdpiUtils.glViewport(0, 0, screenWidth, screenHeight);
		game.batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight);
		game.batch.getTransformMatrix().idt();

		game.batch.begin();

		//background and discard deck location 
		game.batch.draw(background, 0, 0);
		game.batch.draw(checkbox_ui, 375, 10);

		//ui (player/role \n points \n skip button/end turn button) - dif camera?

		render_card(game_logic.player1);
		renderOpponentHands();
		renderDiscardDeck();
		
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		
		game.batch.end();
		game.shape.end();
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
		stage.dispose();
		//texture.dispose();
	}
	@Override
	public void resize(int width, int height)
	{
		stage.getViewport().update(width, height, true);
	}
//===============================================================================
	public void render_card(player player)
	{
		int iter = 0;
		//deals with selecting correct texture -- implement button to select (associate suit/num?)
		for(card card : game_logic.player1.hand)
		{
			temp = new Texture(Gdx.files.internal("Card_Back-88x124.png"));
			switch(card.suit)
			{
				case "diamonds":
				{
					temp = diamonds;
					break;
				}
				case "hearts":
				{
					temp = hearts;
					break;
				}
				case "clubs":
				{
					temp = clubs;
					break;
				}
				case "spades":
				{
					temp = spades;
					break;
				}
				case "joker":
				{
					temp = joker;
					break;
				}
				default:
				{
					//uh oh error
					break;
				}
			}

			//not urgent, but figure out how make cards fixed to their checkboxes
			if(card.value <= 5) //(1-5)
			{
				game.batch.draw(temp, (400 + (iter*90)), 80, (0 + ((card.value - 1)*88)), 0, 88, 124);
			}
			else if(card.value > 5 && card.value <= 10) //(6-10)
			{
				game.batch.draw(temp, (400 + (iter*90)), 80, (0 + ((card.value - 6)*88)), 124, 88, 124);
			}
			else if(card.value > 10 && card.value != 14) //(JQK)
			{
				game.batch.draw(temp, (400 + (iter*90)), 80, (0 + ((card.value - 11)*88)), 248, 88, 124);
			}
			else if(card.value == 14)//joker - different file
			{
				game.batch.draw(temp, (400 + (iter*90)), 80, 88, 0, 88, 124);
			}
			iter++;
		}
		
	}
	public void renderOpponentHands()
	{
		//temp -- adjust specifics later
		//render num of cards in opponent's hands in iso(?)/normal card back
		int iter = 0;
		//temp = new Texture(Gdx.files.internal("Card_Back-88x124.png"));
		temp = card_back_rotated;
		for(card card : game_logic.player2.hand)
		{
			game.batch.draw(temp, 50, ((Gdx.graphics.getHeight()/5) + (iter*45)), 0, 0, 88, 124);
			iter++;
		}
		iter = 0;
		temp = card_back;
		for(card card : game_logic.player3.hand)
		{
			game.batch.draw(temp, (400 + (iter*90)), 870, 0, 0, 88, 124);
			iter++;
		}
		iter = 0;
		temp = card_back_rotated;
		for(card card : game_logic.player4.hand)
		{
			game.batch.draw(temp, 1770, ((Gdx.graphics.getHeight()/5) + (iter*45)), 0, 0, 88, 124);
			iter++;
		}
	}
	public void renderDiscardDeck()
	{
		temp = new Texture(Gdx.files.internal("Card_Back-88x124.png"));
		int iter = 0;
		if(discard_deck.isEmpty())
		{
			game.batch.draw(temp, 1000, 500, 0, 0, 88, 124);
		}
		else
		{
			for(card card : discard_deck)
			{
				switch(card.suit)
				{
					case "diamonds":
					{
						temp = diamonds;
						break;
					}
					case "hearts":
					{
						temp = hearts;
						break;
					}
					case "clubs":
					{
						temp = clubs;
						break;
					}
					case "spades":
					{
						temp = spades;
						break;
					}
					case "joker":
					{
						temp = joker;
						break;
					}
					default:
					{
						//uh oh error
						break;
					}
				}
				if(card.value <= 5) //(1-5)
				{
					game.batch.draw(temp, (1000 + (iter*90)), 500, (0 + ((card.value - 1)*88)), 0, 88, 124);
				}
				else if(card.value > 5 && card.value <= 10) //(6-10)
				{
					game.batch.draw(temp, (1000 + (iter*90)), 500, (0 + ((card.value - 6)*88)), 124, 88, 124);
				}
				else if(card.value > 10 && card.value != 14) //(JQK)
				{
					game.batch.draw(temp, (1000 + (iter*90)), 500, (0 + ((card.value - 11)*88)), 248, 88, 124);
				}
				else if(card.value == 14)//joker - different file
				{
					game.batch.draw(temp, (1000 + (iter*90)), 500, 88, 0, 88, 124);
				}
				iter++;
			}
		}
	}
//===============================================================================
	public void round(logic game_logic)
	{
		roundOrder(game_logic.players); //who starts the chain? How would I get that to work?
		while(!end_of_round)
		{
			while(!end_of_chain)
			{
				if(end_of_turn)
				{
					//player_turns();
					end_of_turn = false;
				}
				//conditions to end chain (no longer can play any cards)
				//check for tycoon bankrupt	
			}
			//set roles
			//conditions to end round(3 players with empty hands)
		}
		assign_points();
	}
	void roundOrder(player[] order)
	{
		//based on role
		if(game_logic.player1.role.equals("commoner"))
		{
			order[0] = game_logic.player1;
			order[1] = game_logic.player2;
			order[2] = game_logic.player3;
			order[3] = game_logic.player1;
			return;
		}
		else
		{
			for(player player : game_logic.players)
			{
				if(player.role.equals("tycoon"))
				{
					order[0] = player;
				}
				else if(player.role.equals("rich"))
				{
					order[1] = player;
				}
				else if(player.role.equals("poor"))
				{
					order[2] = player;
				}
				else //beggar
				{
					order[3] = player;
				}
			}
		}
	}
	void setTycoon(player player)
	{
		player.role = "tycoon";
	}
	void setRich(player player)
	{
		player.role = "rich";
	}
	void setPoor(player player)
	{
		player.role = "poor";
	}
	void setBeggar(player player)
	{
		player.role = "beggar";
	}
	void tycoon_bankrupt(player player, List<card> player_hand)
	{
		//discard rest of hand and remove from play until next round
		player.role = "beggar";
		player_hand.clear();
	}
	void assign_points() //based on roles
	{
		for(player player : game_logic.players)
		{
			if(player.role.equals("tycoon"))
			{
				player.points += 30;
			}
			else if(player.role.equals("rich"))
			{
				player.points += 20;
			}
			else if(player.role.equals("poor"))
			{
				player.points += 10;
			}
			//else - beggar -> 0 points
		}
	}
	public void setHands(logic game_logic)
	{
		game_logic.deck.shuffle();
		game_logic.deck.deal_hand(game_logic.player1, 0);
		game_logic.deck.deal_hand(game_logic.player2, 1);
		game_logic.deck.deal_hand(game_logic.player3, 2);
		game_logic.deck.deal_hand(game_logic.player4, 3);
        discard_deck = new LinkedList<card>();
		for(card card: game_logic.player1.player_hand)
        {
			game_logic.player1.hand.add(card);
        }
		for(card card: game_logic.player2.player_hand)
        {
            game_logic.player2.hand.add(card);
        }
		for(card card: game_logic.player3.player_hand)
        {
            game_logic.player3.hand.add(card);
        }
		for(card card: game_logic.player4.player_hand)
        {
            game_logic.player4.hand.add(card);
        }
	}
	public List<card> discard(String input) //no logic, only discard
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
        	    discard_deck.add(card);
        	    top = card;
        	}
       	}
        return returnArr;
    }
	public boolean checkPower(List<String> input) //only logic, no discard
	{
		boolean passed = false;
		List<card> check = new LinkedList<card>();
		card joker_comp = new card("joker", 14, 14);
		for(String temp : input)
		{
			for(card card : game_logic.player1.hand)
       		{
            	if(temp.equals(card.suit + " " + card.value))
            	{
					if(check.contains(joker_comp)) //potentially temp solution(it works for now) -- need to figure out for entries that are supposed to have multiple jokers
					{
						continue; //ignore the additional joker
					}
    	        	check.add(card);
            	}
        	}
		}
		if(discard_deck.isEmpty())
		{
			//empty discard deck, start of chain
			if(check.size() == 4)
			{
				dialog = new Dialog("Revolution!", skin);
				dialog.setHeight(100);
				dialog.setWidth(250);
				dialog.setPosition(375, 250);
				dialog.text("Card Strength: Reversed.");
				dialog.button("Exit");
				isRevolution = true;
			}
			passed = true;
		}
		else if(check.size() != discard_deck.size())
		{
			//dialog - not same amt of cards
			dialog = new Dialog("Invalid Entry", skin);
			dialog.setHeight(100);
			dialog.setWidth(250);
			dialog.setPosition(375, 250);
			dialog.text("Please enter the same amount of cards as\nthe discarded deck.");
			dialog.button("Exit");
			stage.addActor(dialog);
			card_selection.uncheckAll();
		}
		else if(check.get(0).power < discard_deck.get(0).power)
		{
			//dialog - card is weaker
			dialog = new Dialog("Invalid Entry", skin);
			dialog.setHeight(100);
			dialog.setWidth(250);
			dialog.setPosition(375, 250);
			dialog.text("Please enter a stronger card.");
			dialog.button("Exit");
			stage.addActor(dialog);
			card_selection.uncheckAll();
		}
		else if(check.get(0).value == 8 && check.get(0).power > discard_deck.get(0).power)
		{
			//end card chain (8-stop)
			end_of_chain = true;
			passed = true;
			discard_deck.clear();
			
		}
		else if(check.get(0).value == 3 && check.get(0).suit.equals("spades") && discard_deck.get(0).suit.equals("joker"))
		{
			//3-spade reversal (overpowers)
			passed = true;
			discard_deck.clear();
		}
		else
		{
			//normal play
			passed = true;
			discard_deck.clear();
		}
		//sets discard up with empty discard deck to allow for new set
		return passed;
	}
	public boolean check_revoltPower(List<String> input)
	{
		boolean passed = false;
		List<card> check = new LinkedList<card>();
		card joker_comp = new card("joker", 14, 14);
		for(String temp : input)
		{
			for(card card : game_logic.player1.hand)
       		{
            	if(temp.equals(card.suit + " " + card.value))
            	{
					if(check.contains(joker_comp) && !dupe) //potentially temp solution(it works for now) -- need to figure out for entries that are supposed to have multiple jokers
					{
						continue; //ignore the additional joker
					}
    	        	check.add(card);
            	}
        	}
		}
		if(discard_deck.isEmpty())
		{
			if(check.size() == 4)
			{
				dialog = new Dialog("Counter-Revolution!", skin);
				dialog.setHeight(100);
				dialog.setWidth(250);
				dialog.setPosition(375, 250);
				dialog.text("Card Strength: Normal.");
				dialog.button("Exit");
				isRevolution = false;
			}
			passed = true;
		}
		else if(check.size() != discard_deck.size())
		{
			//dialog - not same amt of cards
			dialog = new Dialog("Invalid Entry", skin);
			dialog.setHeight(100);
			dialog.setWidth(250);
			dialog.setPosition(375, 250);
			dialog.text("Please enter the same amount of cards as\nthe discarded deck.");
			dialog.button("Exit");
			stage.addActor(dialog);
			card_selection.uncheckAll();
		}
		else if(check.get(0).power > discard_deck.get(0).power)
		{
			//dialog - card is weaker
			dialog = new Dialog("Invalid Entry", skin);
			dialog.setHeight(100);
			dialog.setWidth(250);
			dialog.setPosition(375, 250);
			dialog.text("Please enter a weaker card.");
			dialog.button("Exit");
			stage.addActor(dialog);
			card_selection.uncheckAll();
		}
		else if(check.get(0).value == 8 && check.get(0).power < discard_deck.get(0).power)
		{
			//end card chain (8-stop)
			end_of_chain = true;
			passed = true;
			discard_deck.clear();
			
		}
		else
		{
			//normal play
			passed = true;
			discard_deck.clear();
		}
		//sets discard up with empty discard deck to allow for new set
		return passed;
	}
	public void card_swap_dialog()
	{
		//functions similiar to discard, 1 card Poor -> Rich // 2 cards Beggar -> Tycoon
		switch(game_logic.player1.role)
		{
			case "commoner": //nothing happens
			{
				break;
			}
			case "beggar": //give 2 'strong' cards to tycoon
			{
				dialog = new Dialog("Card Swap", skin);
				dialog.setHeight(100);
				dialog.setWidth(250);
				dialog.setPosition(375, 250);
				dialog.text("Please select two \'strong\' cards\nto give to the tycoon:");
				dialog.button("Exit");
				stage.addActor(dialog);
				break;
			}
			case "poor": //give 1 'strong' card to rich
			{
				dialog = new Dialog("Card Swap", skin);
				dialog.setHeight(100);
				dialog.setWidth(250);
				dialog.setPosition(375, 250);
				dialog.text("Please select one \'strong\' cards\nto give to the rich:");
				dialog.button("Exit");
				stage.addActor(dialog);
				break;
			}
			case "rich": //give 1 'weak' card to poor
			{
				dialog = new Dialog("Card Swap", skin);
				dialog.setHeight(100);
				dialog.setWidth(250);
				dialog.setPosition(375, 250);
				dialog.text("Please select one \'weak\' cards\nto give to the poor:");
				dialog.button("Exit");
				stage.addActor(dialog);
				break;
			}
			case "tycoon": //give 2 'weak' cards to beggar
			{
				dialog = new Dialog("Card Swap", skin);
				dialog.setHeight(100);
				dialog.setWidth(250);
				dialog.setPosition(375, 250);
				dialog.text("Please select two \'weak\' cards\nto give to the beggar:");
				dialog.button("Exit");
				stage.addActor(dialog);
				break;
			}
		}
	}
	public List<card> card_swap(String input)
	{
		List<card> returnArr = new LinkedList<card>();
		player_swap = new LinkedList<card>();
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
	public void swap()
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
		
	}
//===============================================================================
	public void player_turns()
	{
		game_logic.player2.hand = player_turn(game_logic.player2.hand);
		currentCard.setText(top.suit + " " + top.value);
		game_logic.player3.hand = player_turn(game_logic.player3.hand);
		currentCard.setText(top.suit + " " + top.value);
		game_logic.player4.hand = player_turn(game_logic.player4.hand);
		currentCard.setText(top.suit + " " + top.value);
	}
	List<card> player_turn(List<card> player_hand)
    {
        //check top - does the hand have anything higher? -- how many cards are in play? -> choose according to these metrics
        //how many cards?
        List<card> viable = new LinkedList<card>();
		boolean end_turn = false;
        //deal with start of chain
        if(discard_deck.isEmpty())
        {
            Random rnd = ThreadLocalRandom.current();
            int index = rnd.nextInt(player_hand.size());
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
        	
            List<card> toDiscard = new LinkedList<card>();
            if(discard_size > 1)
            {
				card compare_prev = viable.get(0);
            	card compare_next = viable.get(0);
                for(int i = 0; i < viable.size(); i++)
                {
                    compare_next = viable.get(i);
                    //System.out.println("\n" + compare_prev.suit + "-" + compare_prev.value + " == " + compare_next.suit + "-" + compare_next.value);
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
                            //System.out.println("Added " + compare_prev.suit + "-" + compare_prev.value);
                        }
                        toDiscard.add(compare_next);
                        //System.out.println("Added " + compare_next.suit + "-" + compare_next.value);
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
            //deal with discards bigger than one card
            List<card> toDiscard = new LinkedList<card>();
            if(discard_size > 1)
            {
				card compare_prev = viable.get(0);
            	card compare_next = viable.get(0);
                for(int i = 0; i < viable.size(); i++)
                {
                    compare_next = viable.get(i);
                    //System.out.println("\n" + compare_prev.suit + "-" + compare_prev.value + " == " + compare_next.suit + "-" + compare_next.value);
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
                            //System.out.println("Added " + compare_prev.suit + "-" + compare_prev.value);
                        }
                        toDiscard.add(compare_next);
                        //System.out.println("Added " + compare_next.suit + "-" + compare_next.value);
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
    List<card> discard(List<card> player_hand, List<card> input)
    {
        //pick card that matched criteria -> right power (rev/no rev), right num of cards, etc. -> pass into discard (no logic, only removing card)
        discard_deck.clear();
        hand_return = new LinkedList<card>();
        Random rnd = ThreadLocalRandom.current();
        int index = rnd.nextInt(input.size());
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
	List<card> card_swap(List<card> player_hand, int num_of_cards, String role)
    {
        ai_swap = new LinkedList<card>();
        for(int i = 0; i < num_of_cards; i++)
        {
            Random rnd = ThreadLocalRandom.current();
            int index;
            if(role.equals("tycoon") || role.equals("rich")) // -> lowerbound
            {
                index = rnd.nextInt(player_hand.size() - 7);
                //System.out.println("Index:" + index);
                ai_swap.add(player_hand.get(index));
                player_hand.remove(index);
            }
            else //beggar || poor -> upperbound
            {
                index = rnd.nextInt(player_hand.size())/2 + 6;
                //System.out.println("Index:" + index);
                ai_swap.add(player_hand.get(index));
                player_hand.remove(index);
            }
        }
        return ai_swap;
    }
//===============================================================================
	//unused - but needed to implement Screen
//===============================================================================
	@Override
	public void hide(){}
	@Override
	public void pause(){}
	@Override
	public void resume(){}
//===============================================================================
}
