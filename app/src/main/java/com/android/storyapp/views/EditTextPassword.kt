package com.android.storyapp.views

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import java.util.regex.Pattern

class EditTextPassword: AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()){
                    // if password length < 6 character or pattern not valid, then show error
                    if (!isValidPassword(s)){
                        error = "Invalid Password"
                    }
                }

            }
            override fun afterTextChanged(s: Editable) {
                // Do nothing.
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    private fun isValidPassword(password: CharSequence?): Boolean {
        // password pattern using Regular Expression
        val regex = ("^(?=.*[0-9])"             // password must contain number
                + "(?=.*[a-z])(?=.*[A-Z])"      // password must contain uppercase and lowercase
                + "(?=\\S+$).{8,}$")            // password must not have whitespace and have length more than 8
        val pattern = Pattern.compile(regex)

        return if (password == null) false else pattern.matcher(password).matches()
    }
}