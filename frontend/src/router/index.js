import { createRouter, createWebHistory } from 'vue-router'

import AllActivities from '../views/activities/AllActivities.vue'
import CreateActivity from '../views/activities/CreateActivity.vue'
import AvailableSlots from '../views/scheduling/AvailableSlots.vue'
import ReserveSlot from '../views/scheduling/ReserveSlot.vue'
import AccountLogin from '../views/account/Login.vue'
import ModerationCases from '../views/moderation/Cases.vue'
import ApplySanction from '../views/moderation/ApplySanction.vue'
import UserRatings from '../views/ratings/UserRatings.vue'
import FacilitySearch from '../views/search/FacilitySearch.vue'
import AllBookings from '../views/bookings/AllBookings.vue'
import CreateBooking from '../views/bookings/CreateBooking.vue'
import BookingFacilitySearch from '../views/bookings/BookingFacilitySearch.vue'

const routes = [
  {
    path: '/',
    redirect: '/activities'
  },
  {
    path: '/activities',
    name: 'AllActivities',
    component: AllActivities
  },
  {
    path: '/activities/new',
    name: 'CreateActivity',
    component: CreateActivity
  },
  {
    path: '/slots',
    name: 'AvailableSlots',
    component: AvailableSlots
  },
  {
    path: '/slots/reserve',
    name: 'ReserveSlot',
    component: ReserveSlot
  },
  {
    path: '/account/login',
    name: 'AccountLogin',
    component: AccountLogin
  },
  {
    path: '/moderation/cases',
    name: 'ModerationCases',
    component: ModerationCases
  },
  {
    path: '/moderation/sanctions/new',
    name: 'ApplySanction',
    component: ApplySanction
  },
  {
    path: '/ratings',
    name: 'UserRatings',
    component: UserRatings
  },
  {
    path: '/search/facilities',
    name: 'FacilitySearch',
    component: FacilitySearch
  },
  {
    path: '/bookings',
    name: 'AllBookings',
    component: AllBookings
  },
  {
    path: '/bookings/new',
    name: 'CreateBooking',
    component: CreateBooking
  },
  {
    path: '/bookings/facilities',
    name: 'BookingFacilitySearch',
    component: BookingFacilitySearch
  },
  {
    // fallback for unknown routes — redirect to activities
    path: '/:catchAll(.*)',
    redirect: '/activities'
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
