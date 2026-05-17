<template>
  <div id="app">
    <header>
      <h1>🏆 SportLink</h1>
      <nav>
        <router-link to="/activities">Activities</router-link> |
        <router-link to="/activities/new">Create Activity</router-link> |
        <router-link to="/slots">Available Slots</router-link> |
        <router-link to="/slots/reserve">Reserve a Slot</router-link> |
        <router-link to="/account/login">Login</router-link> |
        <router-link to="/moderation/cases">Cases</router-link> |
        <router-link to="/moderation/sanctions/new">Apply Sanction</router-link> |
        <router-link to="/ratings">User Ratings</router-link>
        <router-link to="/search/facilities">Facility Search</router-link>
      </nav>
    </header>

    <main>
      <router-view />
    </main>
  </div>
</template>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  max-width: 960px;
  margin: 0 auto;
  padding: 20px;
}

header h1 {
  color: #42b983;
  margin-bottom: 10px;
}

nav {
  padding: 20px;
  background: #f4f4f4;
  border-radius: 8px;
  margin-bottom: 30px;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
  text-decoration: none;
  margin: 0 5px;
}

nav a.router-link-exact-active {
  color: #42b983;
}
</style>