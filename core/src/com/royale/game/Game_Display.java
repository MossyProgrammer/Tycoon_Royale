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
import com.badlogic.gdx.scenes.scene2d.EventListener;
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
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
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
	Label p1_role;
	Label p2_role;
	Label p3_role;
	Label p4_role;

	Texture card_back, card_back_rotated, joker, spades, clubs, hearts, diamonds;
	Texture temp;

	ButtonGroup<CheckBox> card_selection;
	Array<CheckBox> checkbox_reset;
	Array<CheckBox> checked;
	CheckBox skip;
	TextButton submit;
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
	boolean tycoon_set;
	boolean rich_set;
	boolean poor_set;
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
		joker = new Texture(Gdx.files.internal("joker.png")); //temp
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

		p1_role = new Label("Role: " + game_logic.player1.role, skin);
		p1_role.setPosition(700, 20);
		stage.addActor(p1_role);

		p2_role = new Label("Role: " + game_logic.player2.role, skin);
		p2_role.setPosition(50, 150);
		stage.addActor(p2_role);

		p3_role = new Label("Role: " + game_logic.player3.role, skin);
		p3_role.setPosition(400, 850);
		stage.addActor(p3_role);

		p4_role = new Label("Role: " + game_logic.player4.role, skin);
		p4_role.setPosition(1770, 150);
		stage.addActor(p4_role);

		tycoon_set = false;
		rich_set = false;
		poor_set = false;

		//card selection buttons
		outputLabel = new Label("Select a card:", skin);
		outputLabel.setPosition(400, 20);
		stage.addActor(outputLabel);

		currentCard = new Label("Current Card", skin);
		currentCard.setPosition(800, 20);
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
		skip = new CheckBox("Skip Turn", skin);
		skip.setPosition(530, 20);
		stage.addActor(skip);
		//submission/end turn
		submit = new TextButton("Submit", skin);
		submit.setPosition(480, 20);
		submit.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
			{
				if(skip.isChecked())
				{
					skip.setChecked(false);
					game_logic.player1.skipped = true;
					
					Random rnd = ThreadLocalRandom.current();
            		discard_size = rnd.nextInt(4);

					if(!game_logic.player2.hand.isEmpty())
					{
						game_logic.player2.hand = player_turn(game_logic.player2.hand, game_logic.player2);
						currentCard.setText(top.suit + " " + top.value);
						checkHand(game_logic.player2);
					}
					Gdx.graphics.requestRendering();
					if(!game_logic.player3.hand.isEmpty())
					{
						game_logic.player3.hand = player_turn(game_logic.player3.hand, game_logic.player3);
						currentCard.setText(top.suit + " " + top.value);
						checkHand(game_logic.player3);
					}
					Gdx.graphics.requestRendering();
					if(!game_logic.player4.hand.isEmpty())
					{
						game_logic.player4.hand = player_turn(game_logic.player4.hand, game_logic.player4);
						currentCard.setText(top.suit + " " + top.value);
						checkHand(game_logic.player4);
					}
					if(game_logic.player2.skipped && game_logic.player3.skipped && game_logic.player4.skipped)
					{
						end_chain();
					}
					return false;
				}
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
				currentCard.setText(top.suit + " " + top.value);
				Gdx.graphics.requestRendering();
				
				//next person request -> move to next in order
				if(!game_logic.player2.hand.isEmpty())
				{
					game_logic.player2.hand = player_turn(game_logic.player2.hand, game_logic.player2);
					currentCard.setText(top.suit + " " + top.value);
					checkHand(game_logic.player2);
				}
				Gdx.graphics.requestRendering();
				if(!game_logic.player3.hand.isEmpty())
				{
					game_logic.player3.hand = player_turn(game_logic.player3.hand, game_logic.player3);
					currentCard.setText(top.suit + " " + top.value);
					checkHand(game_logic.player3);
				}
				Gdx.graphics.requestRendering();
				if(!game_logic.player4.hand.isEmpty())
				{
					game_logic.player4.hand = player_turn(game_logic.player4.hand, game_logic.player4);
					currentCard.setText(top.suit + " " + top.value);
					checkHand(game_logic.player4);
				}
				Gdx.graphics.requestRendering();
				//end_round();
				if(game_logic.player2.skipped && game_logic.player3.skipped && game_logic.player4.skipped)
				{
					end_chain();
				}
				if(tycoon_set && rich_set && poor_set)
				{
					end_round();
				}
                return true;
            }
        });
		stage.addActor(submit);
		Gdx.graphics.requestRendering();
		
		//assign_points();
		Gdx.graphics.requestRendering();
		//setHands();
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
				game.batch.draw(temp, (400 + (iter*90)), 80, 0, 0, 88, 124);
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
		temp = card_back_rotated; //whyyyy are these always borkeddddddd
		for(card card : game_logic.player2.hand)
		{
			game.batch.draw(temp, 50, ((Gdx.graphics.getHeight()/5) + (iter*50)), 0, 0, 124, 88);
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
			game.batch.draw(temp, 1770, ((Gdx.graphics.getHeight()/5) + (iter*50)), 0, 0, 124, 88);
			iter++;
		}
	}
	public void renderDiscardDeck()
	{
		temp = card_back;
		for(int i = 0; i < 4; i++)
		{
			game.batch.draw(temp, (800 + (i*90)), 500, 0, 0, 88, 124);
		}
		int iter = 0;
		if(!discard_deck.isEmpty())
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
					game.batch.draw(temp, (800 + (iter*90)), 500, (0 + ((card.value - 1)*88)), 0, 88, 124);
				}
				else if(card.value > 5 && card.value <= 10) //(6-10)
				{
					game.batch.draw(temp, (800 + (iter*90)), 500, (0 + ((card.value - 6)*88)), 124, 88, 124);
				}
				else if(card.value > 10 && card.value != 14) //(JQK)
				{
					game.batch.draw(temp, (800 + (iter*90)), 500, (0 + ((card.value - 11)*88)), 248, 88, 124);
				}
				else if(card.value == 14)//joker - different file
				{
					game.batch.draw(temp, (800 + (iter*90)), 500, 0, 0, 88, 124);
				}
				iter++;
			}
		}
	}
//===============================================================================
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
	void tycoon_bankrupt()
	{
		//discard rest of hand and remove from play until next round
		for(player player: game_logic.players)
		{
			if(player.role.equals("tycoon"))
			{
				player.role = "beggar";
				player.hand.clear();
			}
		}
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
	public void checkHand(player player)
	{
		if(player.hand.isEmpty())
		{
			if(tycoon_set)
			{
				if(!player.role.equals("tycoon") || !player.role.equals("commoner"))
				{
					tycoon_bankrupt();
				}
				setTycoon(player);
				tycoon_set = true;
			}
			else if(rich_set)
			{
				setRich(player);
				rich_set = true;
			}
			else if(poor_set)
			{
				setPoor(player);
				poor_set = true;
			}
		}
		if(tycoon_set && rich_set && poor_set && !player.hand.isEmpty())
		{
			setBeggar(player);
			end_of_round = true;
		}
		update_roles();
	}
	void update_roles()
	{
		p1_role.setText("Role: " + game_logic.player1.role);
		p2_role.setText("Role: " + game_logic.player2.role);
		p3_role.setText("Role: " + game_logic.player3.role);
		p4_role.setText("Role: " + game_logic.player4.role);
	}
	void end_chain()
	{
		dialog = new Dialog("End Chain?", skin);
		dialog.setHeight(100);
		dialog.setWidth(250);
		dialog.setPosition(375, 250);
		TextButton ybtn = new TextButton("Yes", skin);
		TextButton nbtn = new TextButton("No", skin);
		ybtn.addListener(new InputListener(){
            @Override
         	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
			{
				discard_deck.clear();
				return true;
			}
		});
		dialog.button(ybtn);
		dialog.button(nbtn);
		stage.addActor(dialog);
	}
	void end_round()
	{
		dialog = new Dialog("End of Round", skin);
		dialog.setHeight(75);
		dialog.setWidth(250);
		dialog.setPosition(375, 250);
		TextButton ybtn = new TextButton("To Next Round", skin);
		ybtn.addListener(new InputListener(){
            @Override
         	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
			{
				discard_deck.clear();
				game_logic.player1.hand.clear();
				game_logic.player2.hand.clear();
				game_logic.player3.hand.clear();
				game_logic.player4.hand.clear();
				assign_points();

				/* setTycoon(game_logic.player1);
				setRich(game_logic.player2);
				setPoor(game_logic.player3);
				setBeggar(game_logic.player4); */

				update_roles();
				tycoon_set = false;
				rich_set = false;
				poor_set = false;
				int i = 0;
				setHands(game_logic);
				//reset checkboxes
				for(CheckBox btn : checkbox_reset)
				{
					btn.setVisible(true);
					btn.setText(game_logic.player1.hand.get(i).suit + " " + game_logic.player1.hand.get(i).value);
					i++;
				}
				card_selection.uncheckAll();
				if(!game_logic.player1.role.equals("commoner")) //!game_logic....equals() (testing purposes its currently ==)
				{
					submit.setVisible(false);
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
							submit.setVisible(true);
							skip.setVisible(true);
							Gdx.graphics.requestRendering();
    	 		           return true;
        		    	}
        			});
					stage.addActor(card_swap);
					Gdx.graphics.requestRendering();
				}
				return true;
			}
		});
		dialog.button(ybtn);
		stage.addActor(dialog);
	}
	//===============================================================================
	List<card> player_turn(List<card> player_hand, player player)
    {
		//if there is time, improve the decision making (this is in it's most basic form)
        //check top - does the hand have anything higher? -- how many cards are in play? -> choose according to these metrics
        //how many cards?
        List<card> viable = new LinkedList<card>();
		player.skipped = false;
		boolean end_turn = false;
        //deal with start of chain
		if(player_hand.isEmpty())
		{
			return player_hand;
		}
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
			if(viable.isEmpty())
			{
				player.skipped = true;
				return player_hand;
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
				top = card;
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
