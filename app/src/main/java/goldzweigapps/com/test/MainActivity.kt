package goldzweigapps.com.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
//import com.goldzweigapps.dsl.quick
import java.util.*

//import com.goldzweigapps.dsl.quick

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        quick {
//            onClick = "".add("")
//        }
    }
    fun String.add(other: String) = this.plus(other)
}
