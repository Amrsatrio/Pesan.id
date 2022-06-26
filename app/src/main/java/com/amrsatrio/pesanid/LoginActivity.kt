package com.amrsatrio.pesanid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)
		/*setContent {
			PesanidTheme {
				// A surface container using the 'background' color from the theme
				Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
					Greeting("Android")
				}
			}
		}*/
	}
}

/*@Composable
fun Greeting(name: String) {
	Text(text = "Hello $name!")
	Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
	Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFDF35)) {
		val scrollState = rememberScrollState()
		Column(modifier = Modifier
			.fillMaxSize()
			.verticalScroll(scrollState)) {
			
		}
		Greeting("Android")
	}
}

@Preview(showBackground = true)
@Composable
fun DeezNuts() {
	LazyColumn {
		items(emptyList<Any>()) { item ->

		}
	}
}

@Preview(showBackground = true)
@Composable
fun TheTitle() {
	Column {
		Text("The Goreng")
		Text("Jl. Mawar No. 6 Lantai 3 Mall Megah")
		Row {
			Text("4.9")
			Text("0.1 km")
		}
	}
}*/

/*
@Preview(showBackground = true)
@Composable
fun TableSelection() {
	Row {
		Text("Pilih Meja")
		DropdownMenu {

		}
	}
}*/
