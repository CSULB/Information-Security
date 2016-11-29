<?php 

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
  
class User extends Model
{
     
     protected $fillable = ['first_name', 'last_name', 'email', 'password_hash'];

     protected $hidden = ['password_hash', 'salt', 'verification_code', 'is_verified'];
     
}
?>