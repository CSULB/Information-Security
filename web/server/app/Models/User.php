<?php

namespace App\Models;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Tymon\JWTAuth\Contracts\JWTSubject as JWTSubject;

class User extends Authenticatable implements JWTSubject {

     protected $fillable = ['first_name', 'last_name', 'email', 'password_hash'];

     protected $hidden = ['password_hash', 'salt', 'verification_code', 'is_verified'];

    /**
     * Get the identifier that will be stored in the subject claim of the JWT
     *
     * @return mixed
     */
    public function getJWTIdentifier(){
        return $this->getKey();
    }

    /**
     * Return a key value array, containing any custom claims to be added to the JWT
     *
     * @return array
     */
    public function getJWTCustomClaims(){
        return [];
    }

}
?>
