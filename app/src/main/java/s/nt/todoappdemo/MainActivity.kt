package s.nt.todoappdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import s.nt.todoappdemo.databinding.ActivityMainBinding
import s.nt.todoappdemo.home.HomeFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        val fm = supportFragmentManager
        fm.beginTransaction()
            .add(R.id.homeContainer, HomeFragment(), HomeFragment::class.java.name)
            .commitNow()
    }
}