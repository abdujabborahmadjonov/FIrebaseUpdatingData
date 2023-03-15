package dev.abdujabbor.firebaseupdatingdata
 class Person {
     var firstName: String? = null
     var lastName: String? = null
     var age: Int = -1

     constructor(firstName: String, lastName: String, age: Int) {
         this.firstName = firstName
         this.lastName = lastName
         this.age = age
     }
     constructor()

     override fun toString(): String {
         return " Name: $firstName\n LastName: $lastName \n Age: $age \n"
     }

 }
