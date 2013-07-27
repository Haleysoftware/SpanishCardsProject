package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import android.content.Context;

public class WordSwapHelper
{
	public static String cateStringToCode (Context ctx, String string)
	{
		String code;
		if (string.matches(ctx.getString(R.string.arrayAll)))
		{
			code = "All";
		}
		else if (string.matches(ctx.getString(R.string.arrayMarked)))
		{
			code = "Marked";
		}
		else if (string.matches(ctx.getString(R.string.arrayLevel)))
		{
			code = "Level";
		}
		else if (string.matches(ctx.getString(R.string.arrayPoints)))
		{
			code = "Points Only";
		}
		else if (string.matches(ctx.getString(R.string.arrayAction)))
		{
			code = "Action";
		}
		else if (string.matches(ctx.getString(R.string.arrayAnimal)))
		{
			code = "Animal";
		}
		else if (string.matches(ctx.getString(R.string.arrayDefine)))
		{
			code = "Define";
		}
		else if (string.matches(ctx.getString(R.string.arrayExpress)))
		{
			code = "Expression";
		}
		else if (string.matches(ctx.getString(R.string.arrayFood)))
		{
			code = "Food";
		}
		else if (string.matches(ctx.getString(R.string.arrayHealth)))
		{
			code = "Health";
		}
		else if (string.matches(ctx.getString(R.string.arrayTalk)))
		{
			code = "Language";
		}
		else if (string.matches(ctx.getString(R.string.arrayNum)))
		{
			code = "Numeral";
		}
		else if (string.matches(ctx.getString(R.string.arrayObject)))
		{
			code = "Object";
		}
		else if (string.matches(ctx.getString(R.string.arrayPeople)))
		{
			code = "Person";
		}
		else if (string.matches(ctx.getString(R.string.arrayPlace)))
		{
			code = "Place";
		}
		else if (string.matches(ctx.getString(R.string.arrayTime)))
		{
			code = "Time";
		}
		else //No clue
		{
			code = null;
		}
		return code;
	}

	public static String cateCodeToString (Context ctx, String code)
	{
		String string;
		if (code.matches("All"))
		{
			string = ctx.getString(R.string.arrayAll);
		}
		else if (code.matches("Marked"))
		{
			string = ctx.getString(R.string.arrayMarked);
		}
		else if (code.matches("Level"))
		{
			string = ctx.getString(R.string.arrayLevel);
		}
		else if (code.matches("Points Only"))
		{
			string = ctx.getString(R.string.arrayPoints);
		}
		else if (code.matches("Action"))
		{
			string = ctx.getString(R.string.arrayAction);
		}
		else if (code.matches("Animal"))
		{
			string = ctx.getString(R.string.arrayAnimal);
		}
		else if (code.matches("Define"))
		{
			string = ctx.getString(R.string.arrayDefine);
		}
		else if (code.matches("Expression"))
		{
			string = ctx.getString(R.string.arrayExpress);
		}
		else if (code.matches("Food"))
		{
			string = ctx.getString(R.string.arrayFood);
		}
		else if (code.matches("Health"))
		{
			string = ctx.getString(R.string.arrayHealth);
		}
		else if (code.matches("Language"))
		{
			string = ctx.getString(R.string.arrayTalk);
		}
		else if (code.matches("Numeral"))
		{
			string = ctx.getString(R.string.arrayNum);
		}
		else if (code.matches("Object"))
		{
			string = ctx.getString(R.string.arrayObject);
		}
		else if (code.matches("Person"))
		{
			string = ctx.getString(R.string.arrayPeople);
		}
		else if (code.matches("Place"))
		{
			string = ctx.getString(R.string.arrayPlace);
		}
		else if (code.matches("Time"))
		{
			string = ctx.getString(R.string.arrayTime);
		}
		else //No Clue
		{
			string = null;
		}
		return string;
	}

	public static String noteCodeToString (Context ctx, String code)
	{
		String string = null;
		if (code.matches("Masculine"))
		{
			string = ctx.getString(R.string.hintM);
		}
		else if (code.matches("Feminine"))
		{
			string = ctx.getString(R.string.hintF);
		}
		else //no hint = 0
		{
			string = "0";
		}

		return string;
	}
}
