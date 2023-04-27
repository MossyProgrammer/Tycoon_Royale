package com.royale.game;
import com.badlogic.gdx.Input.TextInputListener;

public class MyTextInputListener implements TextInputListener 
{
    String user_input;
    @Override
    public void input (String text) 
    {
        user_input = text;
    }

    @Override
    public void canceled ()
    {

    }
}
